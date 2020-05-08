 /******************************************************************************
   Event trace generator
   Copyright (C) 2012 Sylvain Halle
   
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  ******************************************************************************/
 package ca.uqac.info.trace.generation;
 
 import java.util.*;
 
 import org.apache.commons.cli.*;
 
 import ca.uqac.info.trace.*;
 import ca.uqac.info.util.RandomPicker;
 
 import org.w3c.dom.*;
 
 /**
  * Generate a random trace of Amazon ECS shopping cart requests and responses.
  * The constraints followed by the sequence of messages are detailed in:
  * <blockquote>
  * S. Hallé, R. Villemaire. (2012). Constraint-Based Invocation of Stateful Web
  * Services: the Beep Store (Case Study). In
  * <cite>4th International Workshop on
  * Principles of Engineering Service-Oriented Systems (PESOS 2012)</cite>.
  * </blockquote>
  * @author sylvain
  *
  */
 public class AmazonEcsGenerator extends TraceGenerator
 {
 
 	/**
 	 * The seed used to initialize the generator
 	 */
 	protected long m_seed = 0;
 
 	/**
 	 * The random number generator
 	 */
 	protected Random m_random;
 
 	/**
 	 * Intervals for all the aforementioned parameters
 	 */
 	protected int m_minMessages = 1;
 	protected int m_maxMessages = 10;
 	protected int m_maxCartSize = 10;
 	protected int m_catalogSize = 10;
 	protected int m_maxCarts = 1;
 	protected int m_itemsPerOperation = 1;
 	
 	/**
 	 * Maximum price for items (we don't really care
 	 * about that)
 	 */
 	protected static final int MAX_PRICE = 40;
 	
 	/**
 	 * Maximum quantity for items (we don't really care
 	 * about that)
 	 */
 	protected static final int MAX_QTY = 10;
 
 	/**
 	 * Whether to use the system clock as the seed
 	 */
 	protected boolean m_clockAsSeed = false;
 	
 	/**
 	 * Actions one can take on the cart
 	 * @author sylvain
 	 *
 	 */
 	protected enum Action {ITEM_SEARCH, CART_CREATE, CART_CLEAR,
 		CART_ADD, CART_REMOVE, CART_EDIT};
 
 	/**
 	 * Creates a trace generator with default settings
 	 */
 	public AmazonEcsGenerator()
 	{
 		m_random = new Random();
 	}
 
 	/**
 	 * Sets the seed used by the internal random number generator.
 	 * Starting from the same seed and the same input parameters, successive
 	 * calls to generate will always generate the same sequence of
 	 * output traces.
 	 * @param s
 	 */
 	public void setSeed(long s)
 	{
 		m_seed = s;
 		m_random.setSeed(s);
 	}
 
 	@Override
 	public EventTrace generate()
 	{
 		if (m_clockAsSeed)
 			setSeed(System.currentTimeMillis());
 		EventTrace trace = new EventTrace();
 		Vector<Cart> carts = new Vector<Cart>();
 		// Create random pool of items
 		Vector<Item> items = new Vector<Item>();
 		for (int i = 0; i < m_catalogSize; i++)
 			items.add(new Item(i, m_random.nextInt(MAX_PRICE)));
     // We choose the number of messages to produce
     int n_messages = m_random.nextInt(m_maxMessages + 1 - m_minMessages) + m_minMessages;
     for (int i = 0; i < n_messages; i+= 2)
     {
     	Vector<Action> available_operations = new Vector<Action>();
     	// Step 1: determine which operations are available
     	available_operations.add(Action.ITEM_SEARCH);
     	if (carts.size() < m_maxCarts)
     		available_operations.add(Action.CART_CREATE);
     	if (carts.size() > 0)
     		available_operations.add(Action.CART_CLEAR);
     	for (Cart c : carts)
     	{
     		if (c.size() < m_maxCartSize)
     			available_operations.add(Action.CART_ADD);
     		if (c.size() > 0)
     		{
     			available_operations.add(Action.CART_REMOVE);
     			available_operations.add(Action.CART_EDIT);
     		}
     	}
     	// Step 2: pick an operation
     	RandomPicker<Action> action_picker = new RandomPicker<Action>(m_random);
     	Action operation = action_picker.pick(available_operations);
     	// Step 3: create request and response
     	switch (operation)
     	{
     	case ITEM_SEARCH:
     		createItemSearch(trace, carts, items);
     		break;
     	case CART_CREATE:
     		createCartCreate(trace, carts, items);
     		break;
     	case CART_ADD:
     		createCartAdd(trace, carts, items);
     		break;
     	case CART_CLEAR:
     		createCartClear(trace, carts, items);
     		break;
     	case CART_REMOVE:
     		createCartRemove(trace, carts, items);
     		break;
     	case CART_EDIT:
     		createCartEdit(trace, carts, items);
     		break;
     	}
     }
 		return trace;
 	}
 	
 	/**
 	 * Creates a Cart create request-response pair.
 	 * @param trace The trace to which the events will be added
 	 * @param carts The current set of shopping carts
 	 * @param items The current set of catalog items
 	 */
 	private void createCartCreate(EventTrace trace, Vector<Cart> carts, Vector<Item> items)
 	{
 		Cart chosen_cart = new Cart();
 		Vector<Item> items_to_add = new Vector<Item>();
 		// Pick items to add to the cart
 		RandomPicker<Item> item_picker = new RandomPicker<Item>(m_random);
 		for (int i = 0; i < m_itemsPerOperation; i++)
 		{
 			int qty = 1;
 			Item it = item_picker.pick(items);
 			chosen_cart.put(it, qty);
 			items_to_add.add(it);
 		}
 		{ // Create the cart create request
 			Node n = trace.getNode();
 			n.appendChild(createKeyValue(trace, "SessionKey", "0"));
 			n.appendChild(createKeyValue(trace, "Action", "CartCreate"));
 			Node n_items = trace.createElement("Items");
 			for (Item it : items_to_add)
 			{
 				n_items.appendChild(it.toNode(trace));
 			}
 			n.appendChild(n_items);
 			trace.add(new Event(n));
 		}
 		{ // Create the cart create response message
 			Node n = trace.getNode();
 			n.appendChild(createKeyValue(trace, "SessionKey", "0"));
 			n.appendChild(createKeyValue(trace, "Action", "CartCreateResponse"));
 			n.appendChild(createKeyValue(trace, "CartId", chosen_cart.getId()));
 			n.appendChild(chosen_cart.toNode(trace));
 			trace.add(new Event(n));
 		}
 	}
 	
 	/**
 	 * Creates a Cart add request-response pair.
 	 * @param trace The trace to which the events will be added
 	 * @param carts The current set of shopping carts
 	 * @param items The current set of catalog items
 	 */
 	private void createCartAdd(EventTrace trace, Vector<Cart> carts, Vector<Item> items)
 	{
 		// Pick a cart to add to
 		Vector<Cart> eligible_carts = new Vector<Cart>(carts);
 		for (Cart c : carts)
 			if (c.size() < m_maxCartSize)
 				eligible_carts.add(c);
 		RandomPicker<Cart> cart_picker = new RandomPicker<Cart>(m_random);
 		Cart chosen_cart = cart_picker.pick(eligible_carts);
 		// From that cart, pick an item to add
 		Vector<Item> eligible_items = new Vector<Item>();
 		for (Item it : items)
 			if (!chosen_cart.containsKey(it))
 				eligible_items.add(it);
 		RandomPicker<Item> item_picker = new RandomPicker<Item>(m_random);
 		Vector<Item> items_to_add = new Vector<Item>();
 		for (int i = 0; i < m_itemsPerOperation; i++)
 		{
 			int qty = 1;
 			Item it = item_picker.pick(eligible_items);
 			chosen_cart.put(it, qty);
 			items_to_add.add(it);
 		}
 		{ // Create the cart add request
 			Node n = trace.getNode();
 			n.appendChild(createKeyValue(trace, "SessionKey", "0"));
 			n.appendChild(createKeyValue(trace, "Action", "CartAdd"));
 			Node n_items = trace.createElement("Items");
 			for (Item it : items_to_add)
 			{
 				Node n_item = trace.createElement("Item");
 				n_item.appendChild(createKeyValue(trace, "ItemId", it.getId()));
 				n_item.appendChild(createKeyValue(trace, "Quantity", 1));
 				n_items.appendChild(n_item);
 			}
 			n.appendChild(n_items);
 			trace.add(new Event(n));
 		}
 		{ // Create the cart add response message
 			Node n = trace.getNode();
			n.appendChild(trace.createElement("SessionKey").appendChild(trace.createTextNode("0")));
 			n.appendChild(createKeyValue(trace, "Action", "CartAddResponse"));
 			n.appendChild(createKeyValue(trace, "CartId", chosen_cart.getId()));
 			n.appendChild(chosen_cart.toNode(trace));
 			trace.add(new Event(n));
 		}
 	}
 	
 	/**
 	 * Creates an Item Search request-response pair.
 	 * @param trace The trace to which the events will be added
 	 * @param carts The current set of shopping carts
 	 * @param items The current set of catalog items
 	 */
 	private void createItemSearch(EventTrace trace, Vector<Cart> carts, Vector<Item> items)
 	{
 		{ // Create the search request
 			Node n = trace.getNode();
 			n.appendChild(createKeyValue(trace, "SessionKey", "0"));
 			n.appendChild(createKeyValue(trace, "Action", "ItemSearch"));
 			n.appendChild(createKeyValue(trace, "Keyword", "dummy"));
 			trace.add(new Event(n));
 		}
 		{ // Create the search response
 			int num_items = m_random.nextInt(m_maxCartSize);
 			Node n = trace.getNode();
 			n.appendChild(createKeyValue(trace, "SessionKey", "0"));
 			n.appendChild(createKeyValue(trace, "Action", "ItemSearchResponse"));
 			Node n_items = trace.createElement("Items");
 			for (int i = 0; i < num_items; i++)
 			{
 				RandomPicker<Item> item_picker = new RandomPicker<Item>(m_random);
 				Item cart_i = item_picker.pick(items);
 				n_items.appendChild(cart_i.toNode(trace));
				Node n_item =  trace.createElement("Item");
				n_items.appendChild(n_item);
 			}
 			n.appendChild(n_items);
 			trace.add(new Event(n));
 		}
 	}
 	
 	/**
 	 * Creates an cart clear request-response pair.
 	 * @param trace The trace to which the events will be added
 	 * @param carts The current set of shopping carts
 	 * @param items The current set of catalog items
 	 */
 	private void createCartClear(EventTrace trace, Vector<Cart> carts, Vector<Item> items)
 	{
 		// Pick a cart to clear
 		Vector<Cart> eligible_carts = new Vector<Cart>(carts);
 		RandomPicker<Cart> cart_picker = new RandomPicker<Cart>(m_random);
 		Cart chosen_cart = cart_picker.pick(eligible_carts);
 		{ // Create the cart clear message
 			Node n = trace.getNode();
 			n.appendChild(createKeyValue(trace, "SessionKey", "0"));
 			n.appendChild(createKeyValue(trace, "Action", "CartClear"));
 			n.appendChild(createKeyValue(trace, "CartId", chosen_cart.getId()));
 			trace.add(new Event(n));
 		}
 		{ // Create the cart clear response message
 			Node n = trace.getNode();
 			n.appendChild(trace.createElement("SessionKey").appendChild(trace.createTextNode("0")));
 			n.appendChild(createKeyValue(trace, "Action", "CartClearResponse"));
 			n.appendChild(createKeyValue(trace, "CartId", chosen_cart.getId()));
 			trace.add(new Event(n));
 		}
 	}
 	
 	/**
 	 * Creates an cart remove request-response pair.
 	 * @param trace The trace to which the events will be added
 	 * @param carts The current set of shopping carts
 	 * @param items The current set of catalog items
 	 */
 	private void createCartRemove(EventTrace trace, Vector<Cart> carts, Vector<Item> items)
 	{
 		// Pick a cart to remove from
 		Vector<Cart> eligible_carts = new Vector<Cart>();
 		for (Cart c : carts)
 			if (c.size() > 0)
 				eligible_carts.add(c);
 		RandomPicker<Cart> cart_picker = new RandomPicker<Cart>(m_random);
 		Cart chosen_cart = cart_picker.pick(eligible_carts);
 		// From that cart, pick an item to remove
 		RandomPicker<Item> item_picker = new RandomPicker<Item>(m_random);
 		Item chosen_item = item_picker.pick(items);
 		{ // Create the cart remove message
 			Node n = trace.getNode();
 			n.appendChild(createKeyValue(trace, "SessionKey", "0"));
 			n.appendChild(createKeyValue(trace, "Action", "CartRemove"));
 			n.appendChild(createKeyValue(trace, "CartId", chosen_cart.getId()));
 			Node n_items = trace.createElement("Items");
 			Node n_item = trace.createElement("Item");
 			n_item.appendChild(createKeyValue(trace, "ItemId", chosen_item.getId()));
 			n_items.appendChild(n_item);
 			n.appendChild(n_items);
 			trace.add(new Event(n));
 		}
 		// Remove item from cart
 		chosen_cart.remove(chosen_item);
 		{ // Create the cart remove response message
 			Node n = trace.getNode();
 			n.appendChild(createKeyValue(trace, "SessionKey", "0"));
 			n.appendChild(createKeyValue(trace, "Action", "CartRemoveResponse"));
 			n.appendChild(createKeyValue(trace, "CartId", chosen_cart.getId()));
 			n.appendChild(chosen_cart.toNode(trace));
 			trace.add(new Event(n));
 		}
 	}
 	
 	private static Node createKeyValue(EventTrace trace, String key, String value)
 	{
 		Node n = trace.createElement(key);
 		Node v = trace.createTextNode(value);
 		n.appendChild(v);
 		return n;
 	}
 	
 	private static Node createKeyValue(EventTrace trace, String key, Integer value)
 	{
 		return createKeyValue(trace, key, value.toString());
 	}
 	
 	private static Node createKeyValue(EventTrace trace, String key, int value)
 	{
 		return createKeyValue(trace, key, new Integer(value));
 	}
 	
 	/**
 	 * Creates an cart edit request-response pair.
 	 * @param trace The trace to which the events will be added
 	 * @param carts The current set of shopping carts
 	 * @param items The current set of catalog items
 	 */
 	private void createCartEdit(EventTrace trace, Vector<Cart> carts, Vector<Item> items)
 	{
 		// Pick a cart to edit
 		Vector<Cart> eligible_carts = new Vector<Cart>();
 		for (Cart c : carts)
 			if (c.size() > 0)
 				eligible_carts.add(c);
 		RandomPicker<Cart> cart_picker = new RandomPicker<Cart>(m_random);
 		Cart chosen_cart = cart_picker.pick(eligible_carts);
 		// From that cart, pick an item to edit
 		Item chosen_item = chosen_cart.pickItem();
 		Integer new_qty = new Integer(m_random.nextInt(MAX_QTY));
 		{ // Create the cart edit message
 			Node n = trace.getNode(); 
 			n.appendChild(createKeyValue(trace, "SessionKey", "0"));
 			n.appendChild(createKeyValue(trace, "Action", "CartEdit"));
			n.appendChild(trace.createElement("CartId").appendChild(trace.createTextNode(new Integer(chosen_cart.getId()).toString())));
 			Node n_items = trace.createElement("Items");
 			Node n_item = trace.createElement("Item");
			n_item.appendChild(trace.createElement("ItemId").appendChild(trace.createTextNode(new Integer(chosen_item.getId()).toString())));
			n_item.appendChild(trace.createElement("Quantity").appendChild(trace.createTextNode(new Integer(new_qty).toString())));
 			n_items.appendChild(n_item);
 			n.appendChild(n_items);
 			trace.add(new Event(n));
 		}
 		// Edit item in cart
 		chosen_cart.put(chosen_item, new_qty);
 		{ // Create the cart edit response message
 			Node n = trace.getNode();
 			n.appendChild(createKeyValue(trace, "SessionKey", "0"));
 			n.appendChild(createKeyValue(trace, "Action", "CartEditResponse"));
			n.appendChild(trace.createElement("CartId").appendChild(trace.createTextNode(new Integer(chosen_cart.getId()).toString())));
 			n.appendChild(chosen_cart.toNode(trace));
 			trace.add(new Event(n));
 		}
 	}
 
 	@Override
 	public String getAppName()
 	{
 		return "Amazon ECS Shopping Cart Generator";
 	}
 
 	@SuppressWarnings("static-access")
 	@Override
 	public Options getCommandLineOptions()
 	{
 		Options options = new Options();
 		Option opt;
 		options.addOption("t", false, "Use system clock as random generator's seed (default: no)");
 		opt = OptionBuilder.withArgName("x").hasArg().withDescription("Set random generator's seed to x (default: 0)").create("s");
 		options.addOption(opt);
 		opt = OptionBuilder.withArgName("x").hasArg().withDescription("Set maximum number of simultaneous shopping carts to x (default: 1)").create("C");
 		options.addOption(opt);
 		opt = OptionBuilder.withArgName("x").hasArg().withDescription("Set store's catalog size to x (default: 10)").create("i");
 		options.addOption(opt);
 		opt = OptionBuilder.withArgName("x").hasArg().withDescription("Set each cart's maximum size to x (default: 10)").create("k");
 		options.addOption(opt);
     opt = OptionBuilder.withArgName("x").hasArg().withDescription("Maximum number of messages to produce (default: 10)").create("N");
     options.addOption(opt);
     opt = OptionBuilder.withArgName("x").hasArg().withDescription("Minimum number of messages to produce (default: 1)").create("n");
     options.addOption(opt);
 		return options;
 	}
 
 	@Override
 	public void initialize(CommandLine c_line)
 	{
 		if (c_line.hasOption("s"))
 			setSeed(new Integer(c_line.getOptionValue("s")).intValue());
 		if (c_line.hasOption("t"))
 			m_clockAsSeed = true;
 		if (c_line.hasOption("C"))
 			m_maxCarts = new Integer(c_line.getOptionValue("C")).intValue();
 		if (c_line.hasOption("n"))
 			m_minMessages = new Integer(c_line.getOptionValue("n")).intValue();
 		if (c_line.hasOption("N"))
 			m_maxMessages = new Integer(c_line.getOptionValue("N")).intValue();
 		if (c_line.hasOption("k"))
 			m_maxCartSize = new Integer(c_line.getOptionValue("k")).intValue();
 		if (c_line.hasOption("i"))
 			m_catalogSize = new Integer(c_line.getOptionValue("i")).intValue();
 
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		process(args, new AmazonEcsGenerator());
 	}
 	
 	private int cart_id_count = 0;
 	
 	/**
 	 * Basic implementation of an Amazon shopping cart
 	 * @author sylvain
 	 *
 	 */
 	private class Cart extends HashMap<Item,Integer>
 	{
 		/**
      * Mandatory UID (we don't care)
      */
     private static final long serialVersionUID = 1L;
 
 		private int m_id = 0;
 		
 		private Random m_randomGen;
 		
 		public Cart()
 		{
 			super();
 			m_id = cart_id_count++;
 			m_randomGen = AmazonEcsGenerator.this.m_random;
 		}
 		
 		/**
 		 * Randomly pick an item from the cart.
 		 * This method uses the random number generator from the container
 		 * classe (AmazonEcsGenerator).
 		 * @return
 		 */
 		public Item pickItem()
 		{
 			assert size() > 0;
 			Vector<Item> items = new Vector<Item>();
 			items.addAll(super.keySet());
 			int index = m_randomGen.nextInt(items.size());
 			return items.elementAt(index);
 		}
 		
 		public int getId()
 		{
 			return m_id;
 		}
 		
 		public int hashCode()
 		{
 			return m_id;
 		}
 		
 		public boolean equals(Object o)
 		{
 			if (o == null)
 				return false;
 			if (!(o instanceof Cart))
 				return false;
 			return equals((Cart) o);
 		}
 		
 		public boolean equals(Cart c)
 		{
 			if (c == null)
 				return false;
 			return m_id == c.m_id;
 		}
 		
 		public Node toNode(EventTrace t)
 		{
 			Node root = t.createElement("Items");
 			for (Map.Entry<Item,Integer> en : super.entrySet())
 			{
 				Item i = en.getKey();
 				Integer qty = en.getValue();
 				Node n_item = i.toNode(t);
 				n_item.appendChild(createKeyValue(t, "Quantity", qty));
 				root.appendChild(n_item);
 			}
 			return root;
 		}
 	}
 	
 	/**
 	 * Basic implementation of a cart item
 	 * @author sylvain
 	 *
 	 */
 	private class Item
 	{
 		protected int m_id = 0;
 		protected int m_price =0 ;
 		
 		public Item(int id, int price)
 		{
 			super();
 			m_id = id;
 			m_price = price;
 		}
 		
 		public int getId()
 		{
 			return m_id;
 		}
 		
 		public int getPrice()
 		{
 			return m_price;
 		}
 		
 		public int hashCode()
 		{
 			return m_id;
 		}
 		
 		public boolean equals(Object o)
 		{
 			if (o == null)
 				return false;
 			if (!(o instanceof Item))
 				return false;
 			return equals((Item) o);
 		}
 		
 		public boolean equals(Item c)
 		{
 			if (c == null)
 				return false;
 			return m_id == c.m_id;
 		}
 		
 		public Node toNode(EventTrace t)
 		{
 			Node n_item = t.createElement("Item");
 			n_item.appendChild(createKeyValue(t, "ItemId", getId()));
 			n_item.appendChild(createKeyValue(t, "Price", getPrice()));
 			return n_item;
 		}
 	}
 
 }
