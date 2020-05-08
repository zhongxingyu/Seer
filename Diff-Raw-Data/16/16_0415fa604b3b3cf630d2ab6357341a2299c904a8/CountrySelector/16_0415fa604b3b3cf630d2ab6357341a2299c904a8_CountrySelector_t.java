 package ui.graphical;
 
 import game.Country;
 
 import java.util.HashSet;
 import java.util.Set;
 
 class CountrySelector implements MapPanel.ActionListener
 {
 	Set<Country> options;
 
 	Country selection;
 
 	public CountrySelector(Set<Country> options)
 	{
		if (options.size() == 0)
			throw new RuntimeException("Options set is empty");

 		this.options = options;
 	}
 
 	public void actionPerformed(MapPanel.ActionEvent event)
 	{
 		if (event.getCountry() != null && options.contains(event.getCountry()))
 		{
 			setSelection(event.getCountry());

			// Highlight selection
 			Set<Country> highlights = new HashSet<Country>();
 			highlights.add(event.getCountry());
 			event.getSource().setHighlights(highlights);
 		}
 	}
 
 	public boolean hasSelection()
 	{
 		return selection != null;
 	}
 
 	public Country getSelection()
 	{
 		return selection;
 	}
 
 	public void setSelection(Country country)
 	{
 		if (!options.contains(country))
 			throw new RuntimeException("Country not one of the possible options");
 
 		selection = country;
 	}
 
 	static public Country run(Thread gameThread, MapPanel panel, Set<Country> countries)
 	{
 		CountrySelector selector = new CountrySelector(countries);
 		panel.addActionListener(selector);
 		panel.setHighlights(countries);
 
 		try {
 			while (!selector.hasSelection())
 				gameThread.sleep(100);
 		} catch (InterruptedException e) {
 			return null;
 		}
 
 		Country selection = selector.getSelection();
 		panel.removeActionListener(selector);
 		panel.setHighlights(new HashSet<Country>());
 
 		return selection;
 	}
 }
