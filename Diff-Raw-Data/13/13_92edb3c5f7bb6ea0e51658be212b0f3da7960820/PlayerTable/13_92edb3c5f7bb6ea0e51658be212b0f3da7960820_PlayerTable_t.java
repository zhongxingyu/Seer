 package hoplugins.playerCompare;
 
 import plugins.*;
 import hoplugins.PlayerCompare;
 
 //import java.util.*;
 import javax.swing.*;
 import javax.swing.table.*;
 //import java.awt.*;
 
 //import plugins.*;
 /**
  * @author KickMuck
  */
 public class PlayerTable extends JTable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1453037819569111763L;
 	private IHOMiniModel m_HOModel;
 	private int anzCols;
 	private IHOMiniModel m_HOModel1;
 	private int anzCols1;
 	//public PlayerTable(PlayerTableModel tm){
 	public PlayerTable(TableSorter tm, IHOMiniModel iHoMiniMod, PlayerTableModel ptm,boolean small)
 	{
 		super(tm); 
 		m_HOModel1 = iHoMiniMod;
 		PlayerCompare.appendText("Name in PlayerTable: " + ptm.getValueAt(0,1));
 		anzCols1 = tm.getColumnCount();
 	    TableColumn col;
 	    for(int qq = 0; qq < anzCols1; qq++)
 	    {
 	    	int width = 0;
 	    	col = this.getColumnModel().getColumn(qq);
	    	col.setCellRenderer(new MyTableCellRenderer(m_HOModel1));
 	    	PlayerCompare.appendText("Werte in Schleife PlayerTable: " + ptm.getValueAt(0,qq));
 	    	if(qq == 0)
 	    	{
 	    		width = 65;
 	    	}
 	    	else if(qq ==1)
 	    	{
 	    		width = 90;
 	    	}
 	    	else if(qq == 2)
 	    	{
 	    		width = 65;
 	    	}
 	    	col.setPreferredWidth(width);
 	    }
 	    this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 	}
 	public PlayerTable(TableSorter tm, IHOMiniModel iHoMiniMod, PlayerTableModel ptm){
 		
 		super(tm); 
 		m_HOModel = iHoMiniMod;
 		anzCols = tm.getColumnCount();
 	    TableColumn col;
 	
 	    for(int pp = 0; pp < anzCols; pp++)
 	    {
 	    	int width = 0;
 	    	col = this.getColumnModel().getColumn(pp);
 	    	if(pp > 0)
 	    	{
	    		col.setCellRenderer(new MyTableCellRenderer(m_HOModel));
 	    		PlayerCompare.appendText("Werte in Schleife PlayerTable: " + ptm.getValueAt(0,pp));
 	    	}
 	    	if((ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("TOR"))
 	    			|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("IV"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("IVA"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("IVO"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("AV"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("AVI"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("AVO"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("AVD"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("MIT"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("MITD"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("MITA"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("MITO"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("FLG"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("FLGI"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("FLGO"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("FLGD"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("STU"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("STUD"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("Gruppe"))
 	    	)
 	    	{
 	    		width = 60;
 	    	}
 	    	else if((ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("Name"))
 	    			|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("BestePosition"))
 	    			)
 	    	{
 	    		width = 175;
 	    	}
 	    	else if((ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("FUE"))
 	    			|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("ER"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("FO"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("KO"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("TW"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("VE"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("SA"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("PS"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("FL"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("TS"))
 					|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("ST"))
 					)
 	    	{
 	    		width = 40;
 	    	}
 	    	else if((ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("Gehalt"))
 	    			|| (ptm.getColumnName(pp)).equals(m_HOModel.getLanguageString("ID"))
 					|| (ptm.getColumnName(pp)).equals("TSI")
 					)
 	    	{
 	    		width = 80;
 	    	}
 	    	else
 	    	{
 	    		width = 30;
 	    	}
 	    	col.setPreferredWidth(width);
 	    }
 	    
 	    this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 	}
 }
