 package scstool.gui.tab;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionListener;
 import java.text.NumberFormat;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import scstool.gui.comp.ButtonPane;
 import scstool.gui.comp.CustLabel;
 import scstool.obj.Workplace;
 import scstool.proc.CapacityService;
 import scstool.proc.DatabaseContentHandler;
 
 /**
  * 
  * Darstellung der Kapzitaet
  * 
  * @author haeff
  *
  */
 public class CapacityTab extends JPanel 
 {
 
 	private static final long serialVersionUID = 1L;
 
 	private int bnt_var;
 	
 	private CapacityService service;
 	
 	//Button Panel
 	private ButtonPane bnt_pane;
 	
 	private Map<String, JTextField> txtfields;
 	
 	final static Integer ONE_SHIFT = 2400;
 
 	
 	public CapacityTab(int bnt_var)
 	{
 		this.bnt_var = bnt_var;
 		init();
 	}
 	
 	private void init()
 	{
		service = new CapacityService();
 		txtfields = new HashMap<String, JTextField>();
 		buildGui();

 	}
 	
 	private void buildGui()
 	{
 		int rows = 3;
 		int column = 3;
 		
 		setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		c.insets = new Insets(5, 5, 5, 5);
 	    c.gridheight =1;
 	    c.weightx= 1;
 	    c.weighty= 1;
 		
 		
 		//Platzhalter 1. Zeile
 		c.gridx = 0;
 	    c.gridy = 0;
 	    c.gridwidth = column;
 	    c.weightx = 0.0;
 	    c.weighty = 0.0;
 	    add(getTopRow(),c);		
 		
 		//Platzhalter links
 	    c.weightx= 0.0;
 	    c.weighty= 0.0;
 	    c.gridwidth = 1;    
 	    c.gridx = 0;
 	    c.gridy = 1;
 		add(getLeft(),c);
 		
 		//Content mitte
 	    c.weightx= 1.0;
 	    c.weighty= 1.0;
 		c.gridx = 1;
 	    c.gridy = 1;
 		c.fill = GridBagConstraints.NONE;
 		c.anchor = GridBagConstraints.NORTHWEST;
 	    add(getContent(),c);
 		
 	    //Platzhalter rechts
 	    c.fill = GridBagConstraints.BOTH;
 	    c.weightx= 0.0;
 	    c.weighty= 0.0;
 		c.gridx = 2;
 	    c.gridy = 1;
 	    c.anchor = GridBagConstraints.CENTER;
 		add(getRight(),c);
 		
 		//Letzte Zeile
 		c.gridx = 0;
 		c.gridy = rows-1;
 	    c.gridwidth = column;
 	    c.weightx = 0.0;
 	    c.weighty = 0.0;
 	    bnt_pane = new ButtonPane(this.bnt_var);
 	    add(bnt_pane,c);
 		//add(getBottomRow(),c);
 		
 	}
 	
 	/**
 	 * Erstellt den Inhalt des Platzhalters ersten Zeile
 	 * 
 	 * @return
 	 */
 	private JPanel getTopRow()
 	{
 		int width = 0;
 		int height = 50;
 		
 		JPanel pane = new JPanel();
 		pane.setPreferredSize(new Dimension(width,height));
 		
 		//fix fuer einen BUG bezueglich der Breite/Hoehe in einem GribagLayout
 		pane.setMinimumSize(pane.getPreferredSize());
 		
 		return pane;
 	}
 	
 	/**
 	 * Erstellt den Inhalt des Platzhalters letzte Zeile
 	 * 
 	 * @return
 	 */
 /*	private JPanel getBottomRow()
 	{
 		int width = 0;
 		int height = 50;
 		
 		JPanel pane = new JPanel();
 		pane.setPreferredSize(new Dimension(width,height));
 		
 		//fix fuer einen BUG bezueglich der Breite/Hoehe in einem GribagLayout
 		pane.setMinimumSize(pane.getPreferredSize());
 		
 		return pane;
 		
 	}*/
 	
 	/**
 	 * Erstellt den Inhalt des Platzhalters links vom Content
 	 * 
 	 * @return
 	 */
 	private JPanel getLeft()
 	{
 		int width = 100;
 		int height = 0;
 		
 		JPanel pane = new JPanel();
 		pane.setPreferredSize(new Dimension(width,height));
 
 		//fix fuer einen BUG bezueglich der Breite/Hoehe in einem GribagLayout
 		pane.setMinimumSize(pane.getPreferredSize());
 		
 		return pane;
 	}
 	
 	/**
 	 * Erstellt den Inhalt des Platzhalters rechts vom Content
 	 * 
 	 * @return
 	 */
 	private JPanel getRight()
 	{
 		int width = 100;
 		int height = 0;
 		
 		JPanel pane = new JPanel();
 		pane.setPreferredSize(new Dimension(width,height));
 		
 		//fix fuer einen BUG bezueglich der Breite/Hoehe in einem GribagLayout
 		pane.setMinimumSize(pane.getPreferredSize());
 		
 		return pane;
 	}	
 	/**
 	 * Erstellt den Inhalt des Content
 	 * 
 	 * @return
 	 */
 	private JPanel getContent()
 	{
 
 		DatabaseContentHandler dbch = DatabaseContentHandler.get();
 		List<Workplace> wp = dbch.getAllWorkplaces();
 		
 		JPanel pane = new JPanel();
 		pane.setLayout(new GridBagLayout());
 				
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.insets = new Insets(10, 5, 0, 5);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx =1.0;
 		c.gridy = 0;
 		c.gridx = 0;
 
 
 		c.gridwidth = 2;
 		c.anchor = GridBagConstraints.WEST;
 		pane.add(new  CustLabel("Arbeitsplätze"),c);
 		
 		c.gridy=1;	
 		c.gridwidth = 1;
 		pane.add(new JLabel("Nr."),c);
 		
 		c.gridx=1;
 		pane.add(new JLabel("Beschreibung"),c);
 		
 		c.gridx=2;
 		pane.add(new JLabel("Stunden"),c);
 		
 		c.gridx=3;
 		pane.add(new JLabel("Schichten"),c);
 		
 		c.gridx=4;
 		pane.add(new JLabel("Überstunden"),c);
 		
 		c.gridx=5;
 		pane.add(new JLabel("Auslastung"),c);
 		
 		JTextField txt;
 		for(Workplace w: wp)
 		{
 			c.gridy++;
 			c.gridx = 0;
 			String id = w.getId().toString();
 			txt = new JTextField();
 			txt.setText(id);
 			txt.setEditable(false);
 			//txtfields.put(id + "_id", txt);
 			pane.add(txt, c);
 			
 			c.gridx = 1;
 			txt = new JTextField();
 			txt.setText(w.getDescripton());
 			txt.setEditable(false);
 			txt.setPreferredSize(new Dimension(150, 20));
 			txt.setMinimumSize(txt.getPreferredSize());
 			//txtfields.put(id + "_desc", txt);
 			pane.add(txt, c);
 
 			c.gridx = 2;
 			txt = new JTextField();
 			txt.setText("0");
 			txt.setEditable(false);
 			txt.setPreferredSize(new Dimension(30, 20));
 			txt.setMinimumSize(txt.getPreferredSize());
 			txtfields.put(id + "_hours", txt);
 			pane.add(txt, c);
 
 			
 			c.gridx = 3;
 			txt = new JTextField();
 			txt.setText("0");
 			txt.setEditable(false);
 			txt.setPreferredSize(new Dimension(30, 20));
 			txt.setMinimumSize(txt.getPreferredSize());
 			txtfields.put(id + "_shift", txt);
 			pane.add(txt, c);
 
 			
 			c.gridx = 4;
 			txt = new JTextField();
 			txt.setText("0");
 			txt.setEditable(false);
 			txt.setPreferredSize(new Dimension(30, 20));
 			txt.setMinimumSize(txt.getPreferredSize());
 			txtfields.put(id + "_overtime", txt);
 			pane.add(txt, c);
 			
 			c.gridx = 5;
 			txt = new JTextField();
 			txt.setText("0");
 			txt.setEditable(false);
 			txt.setPreferredSize(new Dimension(30, 20));
 			txt.setMinimumSize(txt.getPreferredSize());
 			txtfields.put(id + "_percent", txt);
 			pane.add(txt, c);
 		
 		}
 		return pane;
 	}
 
 	/**
 	 * Gibt den Buttonlistener an das ButtonPanel weiter
 	 * @param l: Actionlistener
 	 */
 	public void addButtonListener(ActionListener l)
 	{
 		bnt_pane.addButtonListener(l);
 	}
 	
 	public void refresh()
 	{
 		LinkedHashMap<Workplace, Integer[]> capa = service.capaciting();
 		
 		for(Map.Entry<Workplace, Integer[]> e : capa.entrySet())
 		{
 				String id = e.getKey().getId().toString();
 				txtfields.get(id+ "_hours").setText(e.getValue()[2].toString());
 				txtfields.get(id+"_shift").setText(e.getValue()[0].toString());
 				txtfields.get(id+ "_overtime").setText(e.getValue()[1].toString());
 				
 				double shifttime = ONE_SHIFT * e.getValue()[0];
 				double hours = e.getValue()[2];
 				NumberFormat nf = NumberFormat.getPercentInstance();
 				txtfields.get(id+ "_percent").setText(nf.format((hours/shifttime)));
 		}
 		
 	}
 }
