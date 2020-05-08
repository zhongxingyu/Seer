 package presentation;
 
 import java.awt.Color;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DragGestureEvent;
 import java.awt.dnd.DragGestureListener;
 import java.awt.dnd.DragSource;
 import java.awt.dnd.DragSourceAdapter;
 import java.awt.dnd.DragSourceDragEvent;
 import java.awt.dnd.DragSourceDropEvent;
 import java.awt.dnd.DragSourceListener;
 import java.awt.dnd.DragSourceMotionListener;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetAdapter;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.io.IOException;
 
 import javax.swing.JPanel;
 
 import controller.CCarte;
 import controller.CColonne;
 import controller.CTasDeCartes;
 
 public class PColonne extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private CColonne c;
 	private PTasDeCartes cachees, visibles;
 
 	// DRAG
 	private DragGestureListener dgl;
 	private DragGestureEvent initialEvent;
 	private DragSource ds;
 	private DragSourceListener dsl;
 	private DragSourceMotionListener dsml;
 	private PTasDeCartes transfer;
 
 	// DROP
 	private DropTarget dt;
 	private DropTargetListener dtl;
 	private DropTargetDropEvent finalEv;
 
 	public PColonne(CColonne _c, PTasDeCartes _cachees, PTasDeCartes _visibles) {
 		this.c = _c;
 		this.cachees = _cachees;
 		this.visibles = _visibles;
 
 		setLayout(null);
 
 		add(visibles);
 		add(cachees);
 		visibles.setOpaque(false);
 
 		// cachees.setSize(new Dimension(80, 1000));
 		// visibles.setSize(cachees.getHeight(), cachees.getHeight() + 25);
 		cachees.setVisible(true);
 		visibles.setVisible(true);
 
 		this.setSize(100, 500);
 		this.setPreferredSize(this.getSize());
 		this.setOpaque(false);
 		setVisible(true);
 
 		// DRAG
 		dgl = new DragGestureListener() {
 			@Override
 			public void dragGestureRecognized(DragGestureEvent dge) {
 				CCarte cc = null;
 				PCarte pc = null;
 				try {
 					initialEvent = dge;
 					pc = (PCarte) visibles.getComponentAt(dge.getDragOrigin());
 					cc = pc.getControle();
 					c.p2c_debutDnD(cc);
 				} catch (Exception e) {
 				}
 			}
 		};
 
 		ds = new DragSource();
 		dsl = new DragSourceAdapter() {
 			@Override
 			public void dragDropEnd(DragSourceDropEvent dsde) {
 				super.dragDropEnd(dsde);
 				try {
 					try {
 						c.p2c_dragDropEnd(
 								dsde.getDropSuccess(),
 								(PTasDeCartes) dsde
 										.getDragSourceContext()
 										.getTransferable()
 										.getTransferData(
 												new DataFlavor(
 														DataFlavor.javaJVMLocalObjectMimeType)));
 					} catch (ClassNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} catch (UnsupportedFlavorException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 
 		dsml = new DragSourceMotionListener() {
 			@Override
 			public void dragMouseMoved(DragSourceDragEvent dsde) {
				int x = dsde.getLocation().x - initialEvent.getDragOrigin().x;
				int y = dsde.getLocation().y - 100 + /* - initialEvent.getDragOrigin().y -*/ ((transfer.getControle().getSize() -1 ) * 10);
 				transfer.setLocation(x, y);
 				repaint();
 			}
 		};
 
 		ds.createDefaultDragGestureRecognizer(visibles,
 				DnDConstants.ACTION_MOVE, dgl);
 		ds.addDragSourceMotionListener(dsml);
 
 		// DROP
 		dtl = new DropTargetAdapter() {
 			@Override
 			public void dragEnter(DropTargetDragEvent dtde) {
 				try {
 					transfer = (PTasDeCartes) dtde
 							.getTransferable()
 							.getTransferData(
 									new DataFlavor(
 											DataFlavor.javaJVMLocalObjectMimeType));
 					c.p2c_dragEnter((CTasDeCartes) transfer.getControle());
 				} catch (UnsupportedFlavorException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 
 			@Override
 			public void dragExit(DropTargetEvent dte) {
 				c.p2c_dragExit((CTasDeCartes) transfer.getControle());
 			}
 
 			@Override
 			public void drop(DropTargetDropEvent dtde) {
 				finalEv = dtde;
 				try {
 					c.p2c_drop((CTasDeCartes) ((PTasDeCartes) dtde
 							.getTransferable()
 							.getTransferData(
 									new DataFlavor(
 											DataFlavor.javaJVMLocalObjectMimeType)))
 							.getControle());
 				} catch (UnsupportedFlavorException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 		dt = new DropTarget(this, dtl);
 	}
 
 	public void empiler(PCarte pc) {
 		visibles.empiler(pc);
 	}
 
 	public void depiler(PCarte pc) {
 		visibles.depiler(pc);
 	}
 
 	// DRAG
 	public void c2p_debutDnDOK(PTasDeCartes transfer) {
 		if (transfer != null) {
 			this.transfer = transfer;
 			getParent().getParent().add(transfer, 0);
 			ds.startDrag(initialEvent, ds.DefaultMoveDrop, transfer, dsl);
 			repaint();
 		}
 	}
 
 	public void c2p_debutDnDNull() {
 
 	}
 
 	// DROP
 	public void c2p_showNeutre() {
 		try {
 			if (!c.isVide())
 				((CCarte) c.getSommet()).getPresentation().setBackground(
 						Color.LIGHT_GRAY);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void c2p_finDnDOK() {
 		finalEv.acceptDrop(DnDConstants.ACTION_MOVE);
 		finalEv.getDropTargetContext().dropComplete(true);
 	}
 
 	public void c2p_finDnDKO() {
 		finalEv.rejectDrop();
 	}
 
 	public void c2p_showEmpilable() {
 		try {
 			if (!c.isVide())
 				((CCarte) c.getSommet()).getPresentation().setBackground(
 						Color.GREEN);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void c2p_showNotEmpilable() {
 		try {
 			if (!c.isVide())
 				((CCarte) c.getSommet()).getPresentation().setBackground(
 						Color.RED);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void initCachees() {
 		cachees.initialiser();
 		visibles.setLocation(cachees.getX(), cachees.getHeight() - 100);
 	}
 }
