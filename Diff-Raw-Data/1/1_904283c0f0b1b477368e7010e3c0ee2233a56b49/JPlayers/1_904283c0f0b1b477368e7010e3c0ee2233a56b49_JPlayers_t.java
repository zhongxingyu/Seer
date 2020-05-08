 package gui.views;
 
 import gui.Language;
 import gui.Main;
 import gui.components.JPlayerCheckBox;
 import gui.popups.ImportPlayers;
 import gui.popups.JPlayerDetails;
 import gui.templates.IconButton;
 import gui.templates.View;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListCellRenderer;
 import javax.swing.filechooser.FileFilter;
 
 import database.players.Double;
 import database.players.Player;
 import database.players.Single;
 import database.players.Team2;
 import database.tournamentParts.Group;
 import database.tournamentParts.Tournament;
 import exceptions.InconsistentStateException;
 import exceptions.InputFormatException;
 
 @SuppressWarnings("serial")
 public class JPlayers extends View implements KeyListener {
 	
 	private boolean enableGroups;
 	private Tournament tournament;
 	private List<Group> groups;
 	private List<Player> unassignedPlayers;
 
 	private JLabel lPlayer, lSearch;
 	
 	private JButton jNewPlayer, jRemovePlayer, jAssignPlayer, jUnassignPlayer,
 			jAddGroup, jDelGroup, jPlayerDown, jPlayerUp, jImport;
 	
 	private JTextField jPlayerName, jFilter;
 	private JList jUnassignedPlayers, jGroupedPlayers;
 	private JComboBox jGroups;
 	
 	class DefaultMouseListener implements MouseListener {
 		@Override
 		public void mouseClicked(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 		}
 	}
 
 	Action addGroup = new AbstractAction() {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			tournament.getQualifying().addGroup();
 			main.refreshState();
 			jGroups.setSelectedIndex(jGroups.getItemCount() - 1);
 		}
 	};
 	
 	Action assignPlayer = new AbstractAction() {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (jUnassignedPlayers.getSelectedValuesList().size() > 0) {
 				Group group = ((Group) jGroups.getSelectedItem());
 				for (Object o : jUnassignedPlayers.getSelectedValuesList()) {
 					Player player = (Player) o;
 					tournament.getQualifying().assignPlayer(player, group);
 				}
 				main.refreshState();
 			}
 		}
 	};
 
 	Action clearSearch = new AbstractAction() {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			jFilter.setText("");
			refreshLists();
 		}
 	};
 
 	Action delGroup = new AbstractAction() {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			unassignedPlayers.addAll(((Group) jGroups.getSelectedItem())
 					.getPlayers());
 			tournament.getQualifying().delGroup(jGroups.getSelectedIndex());
 			main.refreshState();
 		}
 	};
 
 	Action importPlayers = new AbstractAction(Language.get("import")) {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			JFileChooser chooser = new JFileChooser();
 			chooser.setFileFilter(new FileFilter() {
 				@Override
 				public boolean accept(File f) {
 					return f.getName().toLowerCase().endsWith(".csv")
 							| f.getName().toLowerCase().endsWith(".xls")
 							| f.getName().toLowerCase().endsWith(".xlsx")
 							| f.getName().toLowerCase().endsWith(".ods")
 							| f.isDirectory();
 				}
 
 				@Override
 				public String getDescription() {
 					return "Tables (*.csv, *.xls, *.ods, *.xlsx)";
 				}
 			});
 			int returnVal = chooser.showOpenDialog(null);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				String file = chooser.getSelectedFile().getAbsoluteFile()
 						.getAbsolutePath();
 				importPlayers(file);
 				main.refreshState();
 			}
 
 		}
 	};
 	
 	Action playerDown = new AbstractAction() {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			int index = jGroupedPlayers.getSelectedIndex();
 			if (jGroupedPlayers.getSelectedIndex() < ((Group) jGroups
 					.getSelectedItem()).getSize() - 1) {
 				((Group) jGroups.getSelectedItem()).swapPlayers(
 						jGroupedPlayers.getSelectedIndex(),
 						jGroupedPlayers.getSelectedIndex() + 1);
 				index = jGroupedPlayers.getSelectedIndex() + 1;
 			}
 			main.refreshState();
 			jGroupedPlayers.setSelectedIndex(index);
 		}
 	};
 
 	Action playerUp = new AbstractAction() {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			int index = jGroupedPlayers.getSelectedIndex();
 			if (jGroupedPlayers.getSelectedIndex() > 0) {
 				((Group) jGroups.getSelectedItem()).swapPlayers(
 						jGroupedPlayers.getSelectedIndex(),
 						jGroupedPlayers.getSelectedIndex() - 1);
 				index = jGroupedPlayers.getSelectedIndex() - 1;
 			}
 			main.refreshState();
 			jGroupedPlayers.setSelectedIndex(index);
 		}
 	};
 
 	Action refresh = new AbstractAction() {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			main.refreshState();
 		}
 	};
 
 	ListCellRenderer rnd = new ListCellRenderer() {
 		@Override
 		public Component getListCellRendererComponent(JList list, Object value,
 				int index, boolean isSelected, boolean cellHasFocus) {
 			Player plr = (Player) value;
 			final JPlayerCheckBox cb = new JPlayerCheckBox(plr);
 			cb.setOpaque(true);
 			if (isSelected) {
 				cb.setForeground(getBackground());
 				cb.setBackground(getForeground());
 			} else {
 				cb.setBackground(Color.white);
 			}
 			return cb;
 		}
 	};
 
 	Action unassignPlayer = new AbstractAction() {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (!jGroupedPlayers.getSelectedValuesList().isEmpty()) {
 				Group group = ((Group) jGroups.getSelectedItem());
 				for (Object o : jGroupedPlayers.getSelectedValuesList()) {
 					Player player = (Player) o;
 					tournament.getQualifying().unassignPlayer(player, group);
 				}
 				main.refreshState();
 			}
 		}
 	};
 	
 	Action newPlayer = new AbstractAction() {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			String name = jPlayerName.getText();
 			Player player = null;
 			if (name.length() != 0) {
 				String[] splitted = name.split(",");
 				for (int i = 0; i < splitted.length; i++) {
 					while (splitted[i].startsWith(" "))
 						splitted[i] = splitted[i].substring(1);
 				}
 				try {
 					if (tournament.getSingle()) {
 						player = new Single(tournament, splitted);
 					}
 					if (tournament.getDouble()) {
 						player = new Double(tournament, splitted);
 					}
 					if (tournament.get2Team()) {
 						player = new Team2(tournament, splitted);
 					}
 				} catch (InputFormatException e) {
 
 				}
 			}
 			if (player != null) {
 				unassignedPlayers.add(player);
 				jPlayerName.setText("");
 				main.refreshState();
 			} else {
 				jPlayerName.setBackground(Color.RED);
 			}
 		}
 	};
 
 	Action removePlayer = new AbstractAction() {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			if (!jUnassignedPlayers.getSelectedValuesList().isEmpty()) {
 				for (Object o : jUnassignedPlayers.getSelectedValuesList())
 					unassignedPlayers.remove(o);
 				main.refreshState();
 			}
 		}
 	};
 
 	public JPlayers(Main m, boolean enableGroups) {
 		super(m);
 		this.enableGroups = enableGroups;
 		tournament = m.getTournament();
 		unassignedPlayers = tournament.getQualifying().getUnassigned();
 		groups = tournament.getQualifying().getGroups();
 	}
 
 	@Override
 	public void generateWindow() {
 		// non-button elements
 		jUnassignedPlayers = new JList();
 		jUnassignedPlayers.addMouseListener(new DefaultMouseListener(){
 			@Override
 			public void mouseClicked(MouseEvent arg0){
 				double x = arg0.getPoint().getX();
 				switch (arg0.getButton()){
 				case MouseEvent.BUTTON1:
 					if (arg0.getClickCount() == 2) jAssignPlayer.doClick();
 					else if (arg0.getClickCount() == 1) {
 						if (x < 21.0) {
 							int idx1 = jUnassignedPlayers.locationToIndex(arg0.getPoint());
 							if (idx1 != -1) {
 								Player pl = (Player) jUnassignedPlayers.getModel().getElementAt(idx1);
 								pl.setThere(!pl.isThere());
 //								main.setEnabledPattern(getIconEnabledPattern());
 								main.refreshState();
 							}
 						}
 					}
 					break;
 				case MouseEvent.BUTTON2:
 					if (arg0.getClickCount() == 1) {
 						int idx2 = jUnassignedPlayers.locationToIndex(arg0.getPoint());
 						if (idx2 != -1) {
 							Player pl = (Player) jUnassignedPlayers.getModel().getElementAt(idx2);
 							pl.setThere(!pl.isThere());
 							main.setEnabledPattern(getIconEnabledPattern());
 							repaint();
 						}
 					}
 					break;
 				case MouseEvent.BUTTON3:
 					int idx3 = jUnassignedPlayers.locationToIndex(arg0.getPoint());
 					jUnassignedPlayers.setSelectedIndex(idx3);
 					for (Object o : jUnassignedPlayers.getSelectedValuesList()) {
 						new JPlayerDetails(((Player) o).getPersons(), main);
 					}
 					break;
 				}
 			}
 		});
 		jUnassignedPlayers.setCellRenderer(rnd);
 
 		jGroupedPlayers = new JList();
 		jGroupedPlayers.addMouseListener(new DefaultMouseListener() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				double x = arg0.getPoint().getX();
 				switch (arg0.getButton()){
 				case MouseEvent.BUTTON1:
 					if (arg0.getClickCount() == 2) jUnassignPlayer.doClick();
 					else if (arg0.getClickCount() == 1) {
 						if (x < 21.0) {
 							int idx1 = jGroupedPlayers.locationToIndex(arg0.getPoint());
 							if (idx1 != -1) {
 								Player pl = (Player) jGroupedPlayers.getModel().getElementAt(idx1);
 								pl.setThere(!pl.isThere());
 //								main.setEnabledPattern(getIconEnabledPattern());
 								main.refreshState();
 							}
 						}
 					}
 					break;
 				case MouseEvent.BUTTON2:
 					if (arg0.getClickCount() == 1) {
 						int idx2 = jGroupedPlayers.locationToIndex(arg0.getPoint());
 						if (idx2 != -1) {
 							Player pl = (Player) jGroupedPlayers.getModel().getElementAt(idx2);
 							pl.setThere(!pl.isThere());
 							main.setEnabledPattern(getIconEnabledPattern());
 							repaint();
 						}
 					}
 					break;
 				case MouseEvent.BUTTON3:
 					int idx3 = jGroupedPlayers.locationToIndex(arg0.getPoint());
 					if (idx3 != -1) {
 						jGroupedPlayers.setSelectedIndex(idx3);
 						for (Object o : jGroupedPlayers.getSelectedValuesList())
 							new JPlayerDetails(((Player) o).getPersons(), main);
 					}
 					break;
 				}
 			}
 		});
 		jGroupedPlayers.setCellRenderer(rnd);
 
 		jPlayerName = new JTextField() {
 			@Override
 			public Dimension getPreferredSize() {
 				return new Dimension(0, 0);
 			}
 		};
 		jPlayerName.addKeyListener(this);
 		jFilter = new JTextField() {
 			@Override
 			public Dimension getPreferredSize() {
 				return new Dimension(0, 0);
 			}
 		};
 		jFilter.addKeyListener(this);
 		jGroups = new JComboBox(groups.toArray(new Group[0])) {
 			@Override
 			public Dimension getPreferredSize() {
 				return new Dimension(0, 0);
 			}
 		};
 		jGroups.setAction(refresh);
 		jGroups.setSelectedIndex(0);
 		refreshLists();
 
 		// buttons
 		jNewPlayer = new IconButton(newPlayer, "add_small");
 		jRemovePlayer = new IconButton(removePlayer, "remove_small");
 		jAssignPlayer = new IconButton(assignPlayer, "next_small");
 		jUnassignPlayer = new IconButton(unassignPlayer, "back_small");
 		jPlayerUp = new IconButton(playerUp, "up_small");
 		jPlayerDown = new IconButton(playerDown, "down_small");
 		jAddGroup = new IconButton(addGroup, "add_small");
 		jDelGroup = new IconButton(delGroup, "remove_small");
 		jImport = new IconButton(importPlayers, "open") {
 			@Override
 			public Dimension getPreferredSize() {
 				return new Dimension(0, getIcon().getIconHeight());
 			}
 		};
 		;
 
 		// building layout
 		setLayout(new GridBagLayout());
 
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 
 		c.weightx = 0.1;
 		c.weighty = 0;
 		c.gridx = 1;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		add(jPlayerName, c);
 
 		c.weightx = 0;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		lPlayer = new JLabel(Language.get("player"));
 		add(lPlayer, c);
 
 		c.gridx = 3;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		add(jRemovePlayer, c);
 
 		c.gridx = 2;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		add(jNewPlayer, c);
 
 		c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		lSearch = new JLabel(Language.get("search"));
 		add(lSearch, c);
 
 		c.weightx = 0.1;
 		c.gridx = 1;
 		c.gridy = 1;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		add(jFilter, c);
 
 		c.weightx = 0;
 		c.gridx = 3;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		add(new IconButton(clearSearch, "clear_small"), c);
 
 		c.weighty = 0.8;
 		c.weightx = 0.5;
 		c.gridx = 0;
 		c.gridy = 2;
 		c.gridwidth = 4;
 		c.gridheight = 6;
 		JScrollPane sUnassignedPlayers = new JScrollPane(jUnassignedPlayers) {
 			@Override
 			public Dimension getPreferredSize() {
 				return new Dimension(300, 0);
 			}
 		};
 		add(sUnassignedPlayers, c);
 		c.weighty = 0;
 		c.weightx = 0;
 
 		c.weighty = 0.1;
 		c.gridx = 0;
 		c.gridy = 8;
 		c.gridwidth = 4;
 		c.gridheight = 1;
 		add(jImport, c);
 
 		c.weighty = 0;
 		c.gridx = 4;
 		c.gridy = 2;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		add(jAssignPlayer, c);
 
 		c.gridx = 4;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		add(jUnassignPlayer, c);
 
 		c.gridx = 4;
 		c.gridy = 5;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		add(jPlayerUp, c);
 
 		c.gridx = 4;
 		c.gridy = 6;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		add(jPlayerDown, c);
 
 		c.weightx = 0.1;
 		c.gridx = 5;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		add(jGroups, c);
 
 		c.weightx = 0;
 		c.gridx = 6;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		add(jAddGroup, c);
 
 		c.gridx = 7;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		add(jDelGroup, c);
 
 		if (!enableGroups) {
 			jGroups.setVisible(false);
 			jAddGroup.setVisible(false);
 			jDelGroup.setVisible(false);
 		}
 
 		c.weighty = 0.8;
 		c.weightx = 0.5;
 		c.gridx = 5;
 		c.gridy = 1;
 		c.gridwidth = 4;
 		c.gridheight = 8;
 		JScrollPane sGroupedPlayers = new JScrollPane(jGroupedPlayers) {
 			@Override
 			public Dimension getPreferredSize() {
 				return new Dimension(300, 0);
 			}
 		};
 		add(sGroupedPlayers, c);
 		c.weighty = 0;
 	}
 
 	@Override
 	public String getIconEnabledPattern() {
 		boolean quali = tournament.getProperties().DO_QUALIFYING;
 		return isReady() ? (quali ? "1111111111" : "1111101111") : (quali ? "1111111110" : "1111101110");
 	}
 
 	private void importPlayers(String file) {
 		try {
 			main.addWatcher(new ImportPlayers(main, file));
 		} catch (InconsistentStateException e) {
 			System.out.println("ERROR: Tournament state is inconsistent.");
 		}
 	}
 
 	public boolean isReady() {
 		if (groups.isEmpty())
 			return false;
 
 		for (Group g : groups) {
 			if (g.getSize() == 0)
 				return false;
 			for (Player p : g.getPlayers())
 				if (!p.isThere())
 					return false;
 		}
 
 		return true;
 	}
 
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 		if (arg0.getSource() == jPlayerName) {
 			jPlayerName.setBackground(Color.WHITE);
 			if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
 				jNewPlayer.doClick();
 				jPlayerName.setText("");
 			}
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		if ((arg0.getSource() == jFilter))
 			refreshLists();
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 
 	}
 
 	@Override
 	public void refresh() {
 		main.setEnabledPattern(getIconEnabledPattern());
 		refreshLists();
 		repaint();
 	}
 
 	private void refreshLists() {
 		if (groups.size() != jGroups.getModel().getSize()) {
 			DefaultComboBoxModel model = new DefaultComboBoxModel(
 					groups.toArray());
 			jGroups.setModel(model);
 		}
 		List<Player> lst = new ArrayList<Player>();
 		String text = jFilter.getText();
 		text = text.toLowerCase();
 		if (text.equals(""))
 			lst.addAll(unassignedPlayers);
 		else
 			for (Player p : unassignedPlayers) {
 				boolean contains = true;
 				for (String s : text.split(" "))
 					contains = contains
 							& p.getFullName().toLowerCase().contains(s);
 				if (contains)
 					lst.add(p);
 			}
 		jUnassignedPlayers.setListData(lst.toArray());
 		if (jGroups.getSelectedItem() != null)
 			jGroupedPlayers.setListData(((Group) jGroups.getSelectedItem())
 					.getPlayers().toArray(new Player[0]));
 		else
 			jGroupedPlayers.setListData(new int[][] {});
 	}
 
 	@Override
 	public void repaint() {
 		if (lPlayer != null)
 			lPlayer.setText(Language.get("player"));
 		if (lSearch != null)
 			lSearch.setText(Language.get("search"));
 		if (jImport != null)
 			jImport.setText(Language.get("import"));
 		super.repaint();
 	}
 };
