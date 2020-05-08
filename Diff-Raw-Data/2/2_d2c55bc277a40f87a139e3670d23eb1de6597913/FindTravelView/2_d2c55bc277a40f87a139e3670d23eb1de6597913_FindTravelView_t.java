 package padsof.gui.views;
 
 /**
  * 
  */
 import java.rmi.NoSuchObjectException;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 
 import padsof.gui.controllers.Controller;
 import padsof.gui.utils.*;
 import padsof.services.Travel;
 public class FindTravelView extends View
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3047669393550390186L;
 	private JList<Travel> travelList;
 	private JButton btnBook;
 	private JButton btnSearch;
 	private FormGenerator generator;
 
 	public FindTravelView() throws NoSuchObjectException
 	{
 		super("Buscar Viaje Organizado");
 
 		generator = new FormGenerator();
 
 		GroupLayoutHelper midLayoutHelper = new GroupLayoutHelper();
 		btnSearch = new JButton("Buscar");
 		btnBook = new JButton("Reservar");
 		btnBook.setEnabled(false);
 		
 		travelList = new JList<Travel>();
 		
 		travelList.addListSelectionListener(new ListSelectionListener()
 		{
 			@Override
 			public void valueChanged(ListSelectionEvent e)
 			{
 				btnBook.setEnabled(travelList.getSelectedValue() != null);
 			}
 
 		});
 		
 		generator.setTitle("Viajes organizados");
 		generator.addFields("Fecha Inicial", "Fecha Final",
 				"Precio máximo");
 
 		midLayoutHelper.addColumn(generator.generateForm(), btnSearch);
 		midLayoutHelper.addColumn(new JScrollPane(travelList), btnBook);
 
 		JPanel midPanel = new JPanel();
 		
 		midPanel.setLayout(midLayoutHelper.generateLayout(midPanel));
 		GroupLayoutHelper mainLayout = new GroupLayoutHelper();
 		
 		mainLayout.addColumn(Box.createHorizontalStrut(10));
 		mainLayout.addColumn(midPanel);
 		mainLayout.addColumn(Box.createHorizontalStrut(10));
 
 		mainLayout.setInnerMargins(10, 10, 10, 10);
 
 		this.setLayout(mainLayout.generateLayout(this));
 
 	}
 	
 	public Date getStartDate()
 	{
 		return generator.getDatefor("Fecha Inicial");
 	}
 	
 	public Date getEndDate()
 	{
		return generator.getDatefor("Fecha Final");
 	}
 	
 	public String getMaxPrice()
 	{
 		return generator.getValueFor("Precio máximo");
 	}
 	
 	public void setResults(List<Travel> travels)
 	{
 		DefaultListModel<Travel> model = new DefaultListModel<Travel>();
 		
 		for(Travel flight : travels)
 			model.addElement(flight);
 		
 		travelList.setModel(model);
 	}
 
 	@Override
 	public <V extends View> void setController(Controller<V> c)
 	{
 		btnBook.setActionCommand("Book");
 		btnSearch.setActionCommand("Search");
 		
 		btnBook.addActionListener(c);
 		btnSearch.addActionListener(c);
 	}
 
 	public Travel getSelectedTravel()
 	{
 		return travelList.getSelectedValue();
 	}
 }
