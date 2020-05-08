 package net.abusingjava.swing.magic;
 
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.DefaultTableModel;
 
 import org.jdesktop.swingx.JXTable;
 
 import net.abusingjava.swing.MagicPanel;
 import net.abusingjava.xml.XmlAttribute;
 import net.abusingjava.xml.XmlElement;
 
 @XmlElement("multilist")
 public class MultiList extends Table {
 
 	@XmlAttribute("column-head")
 	String $columnHead;
 
 	
 	@Override
 	public void create(final MagicPanel $main, final MagicPanel $parent) {
 
 		super.create($main, $parent);
 		
 		String[] $columnHeaders = new String[] { "", $columnHead };
 		
 		@SuppressWarnings("serial")
 		final JXTable $c = new JXTable(new DefaultTableModel($columnHeaders, 0) {
 			
 			@Override
 			public Class<?> getColumnClass(final int $arg) {
 				if ($arg == 0) {
 					return Boolean.class;
 				}
 				return Object.class;
 			}
 
 			@Override
 			public boolean isCellEditable(final int $row, final int $column) {
 				return $column == 0;
 			}
 		});
 		
 		$c.getColumn("").setMinWidth(22);
 		$c.getColumn("").setMaxWidth(22);
 		$c.setFillsViewportHeight(true);
 		$c.setHorizontalScrollEnabled(true);
 		$c.setSortsOnUpdates(false);
 		$c.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// $c.setCellSelectionEnabled(false);
		// $c.setRowSelectionAllowed(false);
 		
 		/*
 		$c.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(final ListSelectionEvent $ev) {
 				if (!$ev.getValueIsAdjusting()) {
 					int $selected = -1;
 					if ($c.getSelectionModel().isSelectedIndex($ev.getFirstIndex())) {
 						$selected = $ev.getFirstIndex();
 					}
 					if ($c.getSelectionModel().isSelectedIndex($ev.getLastIndex())) {
 						$selected = $ev.getLastIndex();
 					}
 				}
 			}
 		});
 		*/
 		
 		$realComponent = $c;
 		$component = new JScrollPane($c);
 		
 		if ($columnHead == null) {
 			$c.setTableHeader(null);
 			((JScrollPane)$component).setColumnHeader(null);
 		}
 	}
 }
