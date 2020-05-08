 package view.items.minipanels;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import javax.swing.GroupLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 import model.entity.Genre;
 import model.enums.GenreType;
 import settings.GlobalPaths;
 
 /**
  * Klasa symuluje chmurę tagów. Tagi mogą być do niej dodawana pojedynczo lub
  * jako cała {@link ArrayList}. Wewnętrznie korzysta z {@link GenreMiniPanel},
  * który pozwala na wybiór i dodawania pojedynczych gatunków.
  * 
  * @author kornicameister
  * @see GenreMiniPanel
  * 
  */
 public class TagCloudMiniPanel extends JPanel {
 	private static final long serialVersionUID = -8170767178911147951L;
 	private JTable tagsTable = new JTable();
 	private TreeMap<Genre, Integer> genreToRow = new TreeMap<Genre, Integer>();
 	private DefaultTableModel tagsModel;
 	private GenreMiniPanel gmp;
 	private JScrollPane scrollForTable;
 
 	public TagCloudMiniPanel(TreeSet<Genre> genres, GenreType type) {
 		gmp = new GenreMiniPanel(genres, type);
 		this.addPropertyListener();
 		this.initializeTagTable();
 		this.layoutComponents();
 	}
 
 	private void initializeTagTable() {
 		String columnNames[] = { "LP", "ID", "Name" };
 		this.tagsModel = new DefaultTableModel(columnNames, 0);
 		this.tagsTable = new JTable(this.tagsModel);
 		this.tagsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
 		this.tagsTable.setEnabled(false);
 	}
 
 	private void addPropertyListener() {
 		this.gmp.addPropertyChangeListener(new PropertyChangeListener() {
 
 			@Override
 			public void propertyChange(PropertyChangeEvent e) {
 				String property = e.getPropertyName();
 				if (property.equals("genreSelected")
 						|| property.equals("genreCreated")) {
 					addRow((Genre) e.getNewValue());
 				}
 			}
 		});
 	}
 
 	private void layoutComponents() {
 		this.scrollForTable = new JScrollPane(this.tagsTable);
 
 		GroupLayout gl = new GroupLayout(this);
 		this.setLayout(gl);
 
 		gl.setHorizontalGroup(gl.createSequentialGroup()
 				.addComponent(this.gmp, 100, 100, 100).addGap(5)
 				.addComponent(this.scrollForTable));
 
 		gl.setVerticalGroup(gl.createParallelGroup().addComponent(this.gmp)
 				.addComponent(this.scrollForTable, 80, 80, 80));
 
 		this.tagsTable.getColumnModel().getColumn(0).setMaxWidth(40);
 		this.tagsTable.getColumnModel().getColumn(1).setMaxWidth(40);
 	}
 
 	public void addRow(Genre g) {
 		Object data[] = { this.genreToRow.size()+1,null, g.getGenre() };
 		if(g.getPrimaryKey() < 0){
 			data[1] = new JLabel(new ImageIcon(GlobalPaths.CROSS_SIGN.toString()));
 		}
 		this.tagsModel.addRow(data);
 		this.genreToRow.put(g, this.genreToRow.size());
 	}
 
 	public void setTags(Collection<Genre> genres) {
 		gmp.tags.clear();
 		for (Genre g : genres) {
 			gmp.tags.add(g);
			Object data[] = { this.genreToRow.size()+1, g.getPrimaryKey(), g.getGenre() };
 			this.tagsModel.addRow(data);
 			this.genreToRow.put(g, this.genreToRow.size());
 		}
 		this.tagsTable.revalidate();
 	}
 
 	public void removeTag(Genre g) {
 		gmp.tags.remove(g);
 		this.tagsModel.removeRow(this.genreToRow.get(g));
 		this.genreToRow.remove(g);
 		this.tagsTable.revalidate();
 	}
 
 	public void clear() {
 		gmp.tags.clear();
 		this.clearTable();
 		this.genreToRow.clear();
 	}
 
 	private void clearTable(){
 		for (int i = tagsModel.getRowCount(); i > 0; i--) {
 			this.tagsModel.removeRow(i);
 		}
 		this.tagsTable.revalidate();
 	}
 
 	public ArrayList<Genre> getTags() {
 		return gmp.tags;
 	}
 
 	/**
 	 * Wewnętrzna klasa, definiuje funkcjonalność dodawania nowego gatunku bądź
 	 * wybieranie gatunków z tych już istniejących. Nie pozwala na dodanie
 	 * gatunku takiego, który co do nazwy jest identyczny z gatunkiem już
 	 * istniejącym.
 	 * 
 	 * @author kornicameister
 	 * 
 	 */
 	private class GenreMiniPanel extends JPanel implements ActionListener {
 		private static final long serialVersionUID = -9003039695215128758L;
 		private JButton newGenreButton = new JButton("N");
 		private JButton selectGenreButton = new JButton("S");
 		private GenreType type;
 
 		final ArrayList<Genre> tags = new ArrayList<>();
 
 		public GenreMiniPanel(TreeSet<Genre> treeSet, GenreType type) {
 			this.tags.addAll(treeSet);
 			this.initComponents();
 			this.layoutComponents();
 			this.type = type;
 		}
 
 		private void layoutComponents() {
 			JPanel tmp = new JPanel();
 			GroupLayout gl = new GroupLayout(tmp);
 			tmp.setLayout(gl);
 
 			gl.setHorizontalGroup(gl.createParallelGroup()
 					.addComponent(this.newGenreButton, 50, 50, 50)
 					.addComponent(this.selectGenreButton, 50, 50, 50));
 			gl.setVerticalGroup(gl.createSequentialGroup()
 					.addComponent(this.newGenreButton)
 					.addComponent(this.selectGenreButton));
 			this.add(tmp);
 		}
 
 		private void initComponents() {
 			this.newGenreButton.setToolTipText("Create new genre");
 			this.newGenreButton.setName("Create new genre");
 			this.newGenreButton.addActionListener(this);
 			this.selectGenreButton.setToolTipText("Select genre");
 			this.selectGenreButton.setName("Select genre");
 			this.selectGenreButton.addActionListener(this);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			JButton source = (JButton) e.getSource();
 			if (source.equals(newGenreButton)) {
 				String returned = JOptionPane.showInputDialog(null, "Input:",
 						source.getName(), JOptionPane.PLAIN_MESSAGE);
 				if (returned != null) {
 					Genre tmp = new Genre(returned, this.type);
 					int found = Collections.binarySearch(this.tags, tmp,
 							new Comparator<Genre>() {
 								@Override
 								public int compare(Genre o1, Genre o2) {
 									return o1.getGenre().compareTo(
 											o2.getGenre());
 								}
 							});
 					if (found < 0) {
 						this.tags.add(tmp);
 					}
 					this.firePropertyChange("genreCreated", null, tmp);
 				}
 			} else if (source.equals(selectGenreButton)) {
 				if (tags.size() == 0) {
 					return;
 				}
 				Object arr[] = tags.toArray();
 				Object returned = JOptionPane.showInputDialog(source,
 						"Select one from following box", source.getName(),
 						JOptionPane.QUESTION_MESSAGE, null, arr, arr[0]);
 				if (returned != null) {
 					Genre tmp = (Genre) returned;
 					int found = Collections.binarySearch(this.tags, tmp,
 							new Comparator<Genre>() {
 								@Override
 								public int compare(Genre o1, Genre o2) {
 									return o1.getGenre().compareTo(
 											o2.getGenre());
 								}
 							});
 					if (found < 0) {
 						this.firePropertyChange("genreSelected", null, tmp);
 					}
 				}
 			}
 		}
 	}
 }
