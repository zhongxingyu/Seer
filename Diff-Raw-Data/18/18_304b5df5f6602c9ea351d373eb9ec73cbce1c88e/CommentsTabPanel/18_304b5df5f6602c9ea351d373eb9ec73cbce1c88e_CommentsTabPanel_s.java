 package com.robonobo.gui.panels;
 
 import static com.robonobo.gui.GuiUtil.*;
 import static javax.swing.SwingUtilities.*;
 import info.clearthought.layout.TableLayout;
 
 import java.awt.Point;
 import java.awt.event.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.core.api.model.Comment;
 import com.robonobo.core.api.model.User;
 import com.robonobo.core.metadata.CommentCallback;
 import com.robonobo.core.metadata.UserCallback;
 import com.robonobo.gui.RoboColor;
 import com.robonobo.gui.RoboFont;
 import com.robonobo.gui.components.base.*;
 import com.robonobo.gui.frames.RobonoboFrame;
 import com.robonobo.gui.sheets.ConfirmSheet;
 
 @SuppressWarnings("serial")
 public abstract class CommentsTabPanel extends JPanel {
 	static DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm");
 	Set<Long> gotComments = new HashSet<Long>();
 	Map<Long, CommentPanel> pnlsById = new HashMap<Long, CommentPanel>();
 	List<CommentPanel> topLvlCps = new ArrayList<CommentPanel>();
 	JPanel cmtListPnl;
 	JScrollPane cmtListScrollPane;
 	NewCommentForm newCmtForm;
 	RButton newCmtBtn;
 	JPanel newCmtBtnPnl;
 	Log log = LogFactory.getLog(getClass());
 	RobonoboFrame frame;
 
 	public CommentsTabPanel(RobonoboFrame frame) {
 		this.frame = frame;
 		double[][] cellSizen = { { 5, TableLayout.FILL, 0 }, { 5, TableLayout.FILL, 0 } };
 		setLayout(new TableLayout(cellSizen));
 		cmtListPnl = new JPanel();
 		cmtListPnl.setOpaque(false);
 		BoxLayout bl = new BoxLayout(cmtListPnl, BoxLayout.Y_AXIS);
 		cmtListPnl.setLayout(bl);
 		cmtListPnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
 		cmtListScrollPane = new JScrollPane(cmtListPnl);
 		cmtListScrollPane.setOpaque(false);
 		cmtListScrollPane.setBackground(RoboColor.MID_GRAY);
 		add(cmtListScrollPane, "1,1");
 		newCmtBtn = new RGlassButton("Post New Comment...");
 		newCmtBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				CatchingRunnable hideNewCmtForm = new CatchingRunnable() {
 					public void doRun() throws Exception {
 						newCmtForm = null;
 						layoutComments();
 						newCmtBtn.setEnabled(true);
 					}
 				};
 				newCmtForm = new NewCommentForm(hideNewCmtForm, -1);
 				layoutComments();
 				newCmtBtn.setEnabled(false);
 				newCmtForm.textArea.requestFocusInWindow();
 				invokeLater(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						// Scroll to this panel
 						int offset = 0;
 						for (CommentPanel cp : topLvlCps) {
 							offset += cp.getHeight();
 						}
 						cmtListScrollPane.getViewport().setViewPosition(new Point(0, offset));
 					}
 				});
 			}
 		});
 		// Ah, the joys of swing layout
 		newCmtBtnPnl = new JPanel();
 		double[][] ncbpCells = { { newCmtBtn.getPreferredSize().getWidth(), TableLayout.FILL }, { newCmtBtn.getPreferredSize().getHeight() } };
 		newCmtBtnPnl.setLayout(new TableLayout(ncbpCells));
 		newCmtBtnPnl.add(newCmtBtn, "0,0");
 		layoutComments();
 	}
 
 	private void layoutComments() {
 		runOnUiThread(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				cmtListPnl.removeAll();
 				for (CommentPanel cp : topLvlCps) {
 					cmtListPnl.add(cp);
 				}
 				if (newCmtForm != null)
 					cmtListPnl.add(newCmtForm);
 				cmtListPnl.add(newCmtBtnPnl);
 				cmtListScrollPane.revalidate();
 			}
 		});
 	}
 
 	protected abstract boolean canRemoveComment(Comment c);
 
 	protected abstract void newComment(long parentCmtId, String text, CommentCallback cb);
 
 	public void addComments(Collection<Comment> comments) {
 		// Fetch all the userids before showing any of the comments as we need to ensure the comments are loaded in order so that parent comments are always there
 		Set<Long> uids = new HashSet<Long>();
 		for (Comment cmt : comments) {
 			uids.add(cmt.getUserId());
 		}
 		AddCommentCallback cb = new AddCommentCallback(uids, comments);
 		for (Long uid : uids) {
 			frame.ctrl.getOrFetchUser(uid, cb);
 		}
 	}
 
 	/** The height offset from the top of the comment list to just below this comment */
 	private int offsetToComment(CommentPanel cp) {
 		int offset = 0;
 		if (cp.c.getParentId() < 0) {
 			// Top level comment - offset is total height of all top-level cmts up to and including this one
 			for (CommentPanel tlcp : topLvlCps) {
 				offset += tlcp.getHeight();
 				if (tlcp == cp)
 					break;
 			}
 		} else {
 			// Nested comment - offset is total height of all siblings up to and including this cmt, plus
 			// 'internalOffset'
 			// (see below) of parent
 			CommentPanel parent = pnlsById.get(cp.c.getParentId());
 			for (CommentPanel sibling : parent.subPanels) {
 				offset += sibling.getHeight();
 				if (sibling == cp)
 					break;
 			}
 			offset += internalOffset(parent);
 		}
 		offset -= 10; // Otherwise it's a bit off
 		return offset;
 	}
 
 	/** The height of this comment, not including child comments, plus sibling comments above this one, plus the same
 	 * calculation for parents */
 	private int internalOffset(CommentPanel cp) {
 		int result = cp.nameLbl.getHeight() + cp.dateLbl.getHeight() + cp.textLbl.getHeight() + cp.btnsPnl.getHeight() + 15;
 		long parentId = cp.c.getParentId();
 		if (parentId < 0) {
 			// This is a top-level comment
 			for (CommentPanel sibling : topLvlCps) {
 				if (sibling == cp)
 					break;
 				result += sibling.getHeight();
 			}
 		} else {
 			CommentPanel parent = pnlsById.get(parentId);
 			for (CommentPanel sibling : parent.subPanels) {
 				if (sibling == cp)
 					break;
 				result += sibling.getHeight();
 			}
 			result += internalOffset(parent);
 		}
 		return result;
 	}
 
 	public void clear() {
 		// Clears out list of comments (doesn't actually delete the comments themselves, just clears the display
 		gotComments.clear();
 		pnlsById.clear();
 		topLvlCps.clear();
 		layoutComments();
 	}
 	
 	abstract class CommentRemover extends CatchingRunnable {
 		protected CommentPanel cp;
 
 		void doRemove(CommentPanel cp) {
 			this.cp = cp;
 			run();
 		}
 	}
 
 	class AddCommentCallback implements UserCallback {
 		Set<Long> usersToGet = new HashSet<Long>();
 		Collection<Comment> cl;
 		Map<Long, User> gotUsers = new HashMap<Long, User>();
 
 		public AddCommentCallback(Set<Long> usersToGet, Collection<Comment> cl) {
 			this.usersToGet.addAll(usersToGet);
 			this.cl = cl;
 		}
 
 		@Override
 		public void success(User gotUser) {
 			synchronized (this) {
 				long uid = gotUser.getUserId();
 				usersToGet.remove(uid);
 				gotUsers.put(uid, gotUser);
 				if(usersToGet.size() > 0)
 					return;
 			}
 			if(getWidth() == 0)
 				throw new SeekInnerCalmException();
 			
 			runOnUiThread(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					final int totalWidth = getWidth();
 					int topLvlWidth = totalWidth - 20;
 					int indent = 60;
 					for (Comment c : cl) {
 						if(pnlsById.containsKey(c.getCommentId()))
 							continue;
 						User u = gotUsers.get(c.getUserId());
 						boolean canRemove = canRemoveComment(c);
 						CommentRemover remover = null;
 						if (c.getParentId() <= 0) {
 							// Top-level comment
 							if (canRemove) {
 								remover = new CommentRemover() {
 									public void doRun() throws Exception {
 										topLvlCps.remove(cp);
 										pnlsById.remove(cp.c.getCommentId());
 										layoutComments();
 									}
 								};
 							}
 							CommentPanel pnl = new CommentPanel(c, u, topLvlWidth, 0, remover);
 							topLvlCps.add(pnl);
 							pnlsById.put(c.getCommentId(), pnl);
 							layoutComments();
 						} else if (pnlsById.containsKey(c.getParentId())) {
 							// Sub-comment to existing comment
 							final CommentPanel parent = pnlsById.get(c.getParentId());
 							int indentLvl = parent.indentLvl + 1;
 							if (canRemove) {
 								remover = new CommentRemover() {
 									public void doRun() throws Exception {
 										parent.removeSubPanel(cp);
 									}
 								};
 							}
 							CommentPanel pnl = new CommentPanel(c, u, (topLvlWidth - (indentLvl * indent)), indentLvl, remover);
 							pnlsById.put(c.getCommentId(), pnl);
 							parent.addSubPanel(pnl);
 							layoutComments();
 						} else {
 							// Oops
 							log.error("Cannot add comment " + c.getCommentId() + " - no parent comment " + c.getParentId());
 						}
 					}
 				}
 			});
 		}
 
 		@Override
 		public void error(long userId, Exception e) {
 			log.error("Error fetching user " + userId + " for comments");
 		}
 	}
 
 	class CommentPanel extends JPanel {
 		Comment c;
 		User u;
 		List<CommentPanel> subPanels = new ArrayList<CommentPanel>();
 		NewCommentForm newCmtForm;
 		RLabel nameLbl;
 		RLabel dateLbl;
 		JPanel extendoPanel;
 		LineBreakTextPanel textLbl;
 		JPanel btnsPnl;
 		int indentLvl;
 
 		public CommentPanel(Comment c, User u, int totalWidth, int indentLvl, final CommentRemover remover) {
 			this.c = c;
 			this.u = u;
 			this.indentLvl = indentLvl;
 			int textWidth = totalWidth - 80;
 			double[][] cellSizen = { { 0, 50, 10, TableLayout.FILL, 0 }, { 0, 50, TableLayout.FILL, 0 } };
 			setLayout(new TableLayout(cellSizen));
 			add(new JLabel(imgIconFromUrl(u.getImgUrl())), "1,1");
 			extendoPanel = new JPanel();
 			BoxLayout epl = new BoxLayout(extendoPanel, BoxLayout.Y_AXIS);
 			extendoPanel.setLayout(epl);
 			Border botLine = BorderFactory.createMatteBorder(0, 0, 1, 0, RoboColor.DARKISH_GRAY);
 			Border margin = BorderFactory.createEmptyBorder(0, 0, 5, 0);
 			extendoPanel.setBorder(BorderFactory.createCompoundBorder(margin, botLine));
 			nameLbl = new RLabel16B(u.getFriendlyName());
 			nameLbl.setAlignmentX(LEFT_ALIGNMENT);
 			dateLbl = new RLabel11(df.format(c.getDate()));
 			dateLbl.setAlignmentX(LEFT_ALIGNMENT);
 			textLbl = new LineBreakTextPanel(c.getText(), RoboFont.getFont(13, false), textWidth);
 			textLbl.setAlignmentX(LEFT_ALIGNMENT);
 			add(extendoPanel, "3,1,3,2");
 			btnsPnl = new JPanel();
 			btnsPnl.setAlignmentX(LEFT_ALIGNMENT);
 			double[][] btnsCellSizen = { { 80, 10, 100, TableLayout.FILL }, { 5, 30, 5 } };
 			btnsPnl.setLayout(new TableLayout(btnsCellSizen));
 			RButton replyBtn = new RGlassButton("Reply");
 			replyBtn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					CatchingRunnable hideNewCmt = new CatchingRunnable() {
 						public void doRun() throws Exception {
 							newCmtForm = null;
 							relayoutPanel();
 						}
 					};
 					final int scrollOffset = offsetToComment(CommentPanel.this);
 					newCmtForm = new NewCommentForm(hideNewCmt, CommentPanel.this.c.getCommentId());
 					relayoutPanel();
 					newCmtForm.textArea.requestFocusInWindow();
 					invokeLater(new CatchingRunnable() {
 						public void doRun() throws Exception {
 							// Scroll to this form
 							cmtListScrollPane.getViewport().setViewPosition(new Point(0, scrollOffset));
 						}
 					});
 				}
 			});
 			btnsPnl.add(replyBtn, "0,1");
 			if (remover != null) {
				RButton removeBtn = new RRedGlassButton("Remove");
 				final CatchingRunnable doRemove = new CatchingRunnable() {
 					public void doRun() throws Exception {
 						remover.doRemove(CommentPanel.this);
 						frame.ctrl.deleteComment(CommentPanel.this.c, new CommentCallback() {
 							public void success(Comment c) {
 								// Do nothing
 							}
 
 							public void error(long commentId, Exception ex) {
 								log.error("Error deleting comment", ex);
 							}
 						});
 					}
 				};
				removeBtn.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
						frame.showSheet(new ConfirmSheet(frame, "Delete comment?", "Are you sure you want to delete this comment and all replies?", "Remove", doRemove));
 					}
 				});
				btnsPnl.add(removeBtn, "2,1");
 			}
 			relayoutPanel();
 		}
 
 		public void addSubPanel(CommentPanel sp) {
 			subPanels.add(sp);
 			relayoutPanel();
 		}
 
 		public void removeSubPanel(CommentPanel cp) {
 			subPanels.remove(cp);
 			relayoutPanel();
 		}
 
 		private void relayoutPanel() {
 			runOnUiThread(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					extendoPanel.removeAll();
 					extendoPanel.add(nameLbl);
 					extendoPanel.add(dateLbl);
 					extendoPanel.add(Box.createVerticalStrut(5));
 					extendoPanel.add(textLbl);
 					extendoPanel.add(Box.createVerticalStrut(5));
 					extendoPanel.add(btnsPnl);
 					extendoPanel.add(Box.createVerticalStrut(5));
 					for (JPanel sp : subPanels) {
 						sp.setAlignmentX(LEFT_ALIGNMENT);
 						extendoPanel.add(sp);
 					}
 					if (newCmtForm != null) {
 						newCmtForm.setAlignmentX(LEFT_ALIGNMENT);
 						extendoPanel.add(newCmtForm);
 					}
 					extendoPanel.revalidate();
 				}
 			});
 		}
 	}
 
 	class NewCommentForm extends JPanel {
 		RTextArea textArea;
 		RButton postBtn;
 
 		public NewCommentForm(final Runnable remover, final long parentCmtId) {
 			double[][] cellSizen = { { 0, 50, 10, TableLayout.FILL, 100, 10, 100, 10 }, { 0, 25, 0, 60, 10, 30, 10 } };
 			setLayout(new TableLayout(cellSizen));
 			User me = frame.ctrl.getMyUser();
 			add(new JLabel(imgIconFromUrl(me.getImgUrl())), "1,1,1,3,LEFT,TOP");
 			String lblText = (parentCmtId < 0) ? "Post New Comment" : "Post Reply";
 			add(new RLabel14B(lblText), "3,1,LEFT,TOP");
 			textArea = new RTextArea(3, 50);
 			textArea.addKeyListener(new KeyAdapter() {
 				public void keyReleased(KeyEvent e) {
 					postBtn.setEnabled(textArea.getText().length() > 0);
 				}
 			});
 			add(new JScrollPane(textArea), "3,3,6,3");
 			RButton cancelBtn = new RRedGlassButton("Cancel");
 			cancelBtn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					remover.run();
 				}
 			});
 			add(cancelBtn, "4,5");
 			postBtn = new RGlassButton("Post");
 			postBtn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					postBtn.setText("Posting...");
 					postBtn.setEnabled(false);
 					newComment(parentCmtId, textArea.getText(), new CommentCallback() {
 						public void success(Comment c) {
 							runOnUiThread(remover);
 						}
 
 						public void error(long commentId, Exception ex) {
 							// Should never happen
 							runOnUiThread(remover);
 							log.error("Error posting comment", ex);
 						}
 					});
 				}
 			});
 			postBtn.setEnabled(false);
 			add(postBtn, "6,5");
 		}
 	}
 }
