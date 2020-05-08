 package ted.ui.addshowdialog;
 import com.jgoodies.forms.layout.CellConstraints;
 
 import java.awt.Canvas;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Vector;
 
 import com.jgoodies.forms.layout.FormLayout;
 
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 
 import ted.TedTableProgressbarRenderer;
 import ted.datastructures.StandardStructure;
 import ted.ui.TableRenderer;
 
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class EpisodeChooserPanel extends JPanel
 {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8763021531797512857L;
 	private JTable episodesTable;
 	private JScrollPane episodesScrollPane;
 	private EpisodesTableModel episodesTableModel = new EpisodesTableModel();
 
 	private Canvas activityCanvas;
 	TedTableProgressbarRenderer ttpr;
 	
 	private StandardStructure selectedStructure;
 
 	public EpisodeChooserPanel()
 	{
 		//this.getContentPane().add(getEpisodesScrollPane());
 		this.initGUI();
 	}
 	
 	public void episodesTableMouseClicked(MouseEvent evt)
 	{
 		int viewRow = episodesTable.getSelectedRow();
 		int selectedRow = viewRow;
 		//int selectedRow = episodesTable.convertRowIndexToModel(viewRow);
 		if (selectedRow >= 0)
 		{
 			selectedStructure = episodesTableModel.getStandardStructureAt(selectedRow);
 		
 			// TODO: if double click, add the show with selected season/episode
 			// beware, this panel is used in multiple dialogs. make sure they all implement the
 			// callback function
 			if (evt.getClickCount() > 1)
 			{
 			
 			}
 		}
 		
 		
 	}
 
 	private JTable getEpisodesTable() 
 	{
 		if (episodesTable == null) {
 			
 			episodesTable = new JTable();
 			episodesTable.setModel(episodesTableModel);
 			
 			episodesTable.setAutoCreateColumnsFromModel(true);
 			episodesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
 			episodesTable.setEditingRow(0);
 			
 			//	disable horizontal lines in table
 			episodesTable.setShowHorizontalLines(false);
 			episodesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			
 			episodesTable.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent evt) {
 					episodesTableMouseClicked(evt);
 				}});
 			ttpr = new TedTableProgressbarRenderer(0, 100);
 			ttpr.setStringPainted(false);
 			
 			TableRenderer tr = new TableRenderer();
 			episodesTable.setDefaultRenderer(Object.class, tr);
 			episodesTable.setRowHeight(episodesTable.getRowHeight()+5);
 			
 			episodesTable.setDefaultRenderer(JProgressBar.class, ttpr);
 		}
 		return episodesTable;
 	}
 	
 	private JScrollPane getEpisodesScrollPane() 
 	{
 		if (episodesScrollPane == null) {
 			episodesScrollPane = new JScrollPane();
 			episodesScrollPane.setViewportView(getEpisodesTable());
 		}
 		return episodesScrollPane;
 	}
 	
 	private void initGUI() {
 		try {
 			
 			FormLayout thisLayout = new FormLayout(
 				"25dlu:grow, 16px, 25dlu:grow",
 				"max(p;15dlu), 30dlu:grow");
 			this.setLayout(thisLayout);
 			this.add(getEpisodesScrollPane(), new CellConstraints("1, 1, 3, 2, default, default"));
 			this.add(getActivityCanvas(), new CellConstraints("2, 2, 1, 1, default, default"));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setSeasonEpisodes(Vector seasonEpisodes)
 	{
 		this.episodesTableModel.setSeasonEpisodes(seasonEpisodes);	
 		ttpr.setMaximum(this.episodesTableModel.getMaxQuality());
 	}
 
 	public void clear()
 	{
 		this.episodesTableModel.clear();
 		
 	}
 	
 	private Canvas getActivityCanvas() {
 		if (activityCanvas == null) {
 			activityCanvas = new ImageCanvas("icons/activity.gif");
 			activityCanvas.setPreferredSize(new java.awt.Dimension(16, 16));
 			activityCanvas.setBackground(this.episodesTable.getBackground());
 		}
 		return activityCanvas;
 	}
 	
 	public void setActivityStatus(boolean active)
 	{
 		this.activityCanvas.setVisible(active);
 	}
 
 	public StandardStructure getSelectedStructure() 
 	{
 		// TODO: display error message when no structure is selected?
 		return this.selectedStructure;
 	}
 }
