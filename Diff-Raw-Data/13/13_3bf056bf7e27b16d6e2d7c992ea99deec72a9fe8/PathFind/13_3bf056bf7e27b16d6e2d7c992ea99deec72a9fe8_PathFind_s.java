 /*
  *  PathFind -- a Diamond system for pathology
  *
  *  Copyright (c) 2008 Carnegie Mellon University
  *  All rights reserved.
  *
  *  PathFind is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, version 2.
  *
  *  PathFind is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with PathFind. If not, see <http://www.gnu.org/licenses/>.
  *
  *  Linking PathFind statically or dynamically with other modules is
  *  making a combined work based on PathFind. Thus, the terms and
  *  conditions of the GNU General Public License cover the whole
  *  combination.
  *
  *  In addition, as a special exception, the copyright holders of
  *  PathFind give you permission to combine PathFind with free software
  *  programs or libraries that are released under the GNU LGPL or the
  *  Eclipse Public License 1.0. You may copy and distribute such a system
  *  following the terms of the GNU GPL for PathFind and the licenses of
  *  the other code concerned, provided that you include the source code of
  *  that other code when and as the GNU GPL requires distribution of source
  *  code.
  *
  *  Note that people who make modified versions of PathFind are not
  *  obligated to grant this special exception for their modified versions;
  *  it is their choice whether to do so. The GNU General Public License
  *  gives permission to release a modified version without this exception;
  *  this exception also makes it possible to release a modified version
  *  which carries forward this exception.
  */
 
 package edu.cmu.cs.diamond.pathfind;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import edu.cmu.cs.diamond.opendiamond.Filter;
 import edu.cmu.cs.diamond.opendiamond.FilterCode;
 import edu.cmu.cs.diamond.opendiamond.Scope;
 import edu.cmu.cs.diamond.opendiamond.ScopeSource;
 import edu.cmu.cs.diamond.opendiamond.Search;
 import edu.cmu.cs.diamond.opendiamond.Searchlet;
 import edu.cmu.cs.diamond.wholeslide.Wholeslide;
 import edu.cmu.cs.diamond.wholeslide.gui.WholeslideView;
 
 public class PathFind extends JFrame {
 
 	private final SearchPanel searchPanel;
 
 	private final JPanel selectionPanel;
 
 	private final JList savedSelections;
 
 	private final Scope scope;
 
 	private DefaultListModel ssModel;
 
 	private final QueryPanel qp = new QueryPanel(this);
 
 	private final PairedSlideView psv = new PairedSlideView();
 
 	public PathFind(String filename) {
 		super("PathFind");
 		setSize(1000, 750);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// slides in middle
 		add(psv);
 
 		// query bar at bottom
 		add(qp, BorderLayout.SOUTH);
 
 		// search results at top
 		searchPanel = new SearchPanel(this);
 		searchPanel.setVisible(false);
 		add(searchPanel, BorderLayout.NORTH);
 
 		// save selections at left
 		selectionPanel = new JPanel(new BorderLayout());
 		savedSelections = new JList();
 		savedSelections.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		savedSelections.setLayoutOrientation(JList.VERTICAL);
 		selectionPanel.add(new JScrollPane(savedSelections,
 				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
 		selectionPanel.setBorder(BorderFactory
 				.createTitledBorder("Saved Selections"));
 		selectionPanel.setPreferredSize(new Dimension(280, 100));
 		savedSelections.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				Shape selection = (Shape) savedSelections.getSelectedValue();
 				psv.getLeftSlide().setSelection(selection);
 			}
 		});
 		add(selectionPanel, BorderLayout.WEST);
 
 		setLeftSlide(new Wholeslide(new File(filename)), filename);
 
 		// fake menus for now
 		JMenuBar mb = new JMenuBar();
 		setJMenuBar(mb);
 
 		JMenu searchMenu = new JMenu("Search");
 		JMenuItem searchMenuItem = new JMenuItem("ImageJ Search");
 		searchMenuItem.addActionListener(new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				doImageJSearch();
 			}
 		});
 
 		searchMenu.add(searchMenuItem);
 		mb.add(searchMenu);
 
 		// load scope
 		ScopeSource.commitScope();
 		List<Scope> scopes = ScopeSource.getPredefinedScopeList();
 		this.scope = scopes.get(0);
 	}
 
 	/* XXX IMPORTED XXX */
 
 	public void doImageJSearch() {
 		// search
 		/* DEATH BY CAST */
 		Shape s = (Shape) savedSelections.getSelectedValue();
 
 		if (s != null) {
 			// start a search
 			startSearch(s);
 			return;
 		}
 	}
 
 	public void startSearch(Shape shape) {
 		System.out.println("start search");
 
 		Search search = Search.getSharedInstance();
 		// TODO fill in search parameters
 		search.setScope(scope);
 		search.setSearchlet(prepareSearchlet(shape));
 
 		searchPanel.beginSearch(search);
 	}
 
 	private Searchlet prepareSearchlet(Shape shape) {
 		// set up the rgb filter
 		Filter rgb = null;
 		try {
 			FilterCode c = new FilterCode(new FileInputStream(
 					"/opt/snapfind/lib/fil_rgb.a"));
 			rgb = new Filter("RGB", c, "f_eval_img2rgb", "f_init_img2rgb",
 					"f_fini_img2rgb", 1, new String[0], new String[0], 400);
 			System.out.println(rgb);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// init diamond
 		Search search = Search.getSharedInstance();
 		search.setScope(scope);
 
 		// make a new searchlet
 		Searchlet searchlet = new Searchlet();
 		searchlet.addFilter(rgb);
 		searchlet.setApplicationDependencies(new String[] { "RGB" });
 
 		return searchlet;
 	}
 
 	/* XXX END IMPORTED XXX */
 
 	private void setLeftSlide(Wholeslide wholeslide, String title) {
 		final WholeslideView wv = createNewView(wholeslide, title);
 
 		psv.setLeftSlide(wv);
 		wv.getInputMap()
 				.put(KeyStroke.getKeyStroke("INSERT"), "save selection");
 		wv.getActionMap().put("save selection", new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				saveSelection(wv);
 			}
 		});
 		ssModel = new SavedSelectionModel(wv);
 		savedSelections.setModel(ssModel);
 		savedSelections.setCellRenderer(new SavedSelectionCellRenderer(wv));
 	}
 
 	private void setRightSlide(Wholeslide wholeslide, String title) {
 		if (wholeslide == null) {
 			psv.setRightSlide(null);
 		} else {
 			psv.setRightSlide(createNewView(wholeslide, title));
 		}
 	}
 
 	protected void saveSelection(WholeslideView wv) {
 		Shape s = wv.getSelection();
 		if (s != null) {
 			ssModel.addElement(s);
 		}
 	}
 
 	private WholeslideView createNewView(Wholeslide wholeslide, String title) {
 		WholeslideView wv = new WholeslideView(wholeslide);
 		wv.setBorder(BorderFactory.createTitledBorder(title));
 		return wv;
 	}
 
 	public static void main(String[] args) {
 		PathFind pf = new PathFind(args[0]);
 		pf.setVisible(true);
 	}
 
 	public BufferedImage getSelectionAsImage() {
 		Shape s = psv.getLeftSlide().getSelection();
 		if (s == null) {
 			return null;
 		}
 
 		Rectangle2D bb = s.getBounds2D();
 		if (bb.getWidth() * bb.getHeight() > 6000 * 6000) {
 			throw new SelectionTooBigException();
 		}
 
 		BufferedImage img = new BufferedImage((int) bb.getWidth(), (int) bb
 				.getHeight(), BufferedImage.TYPE_INT_RGB);
 
 		Graphics2D g = img.createGraphics();
 		g.setBackground(Color.WHITE);
 		g.clearRect(0, 0, img.getWidth(), img.getHeight());
 		g.clip(s);
		psv.getLeftSlide().getWholeslide().paintRegion(g, 0, 0, (int) bb.getX(),
				(int) bb.getY(), img.getWidth(), img.getHeight(), 1.0);
 		g.dispose();
 
 		return img;
 	}
 }
