 
 package bgpp2011;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Collection;
 import java.util.Iterator;
 
 public class Controller
 {
 	private HashMap<Integer, VehicleType> types;
 	private HashMap<Integer, Vehicle> vehicles;
 	private HashMap<Integer, Customer> customers;
 	private HashMap<Integer, Reservation> reservations;
 	private Nexus nexus;
 	/*
 	 * Controlleren must execute operations given by the user. It must do all the checking and ordering.
 	 * 
 	 * The checkreservation() method checks if there is an overlap with another reservation. It takes a
 	 * reservation as a parameter. First, it checks whether the startdate is before the enddate. Second,
 	 * It checks whether the Vehicle of the Reservations re is the same as i the ArrayList. Then it checks
 	 * whether the startdate is after the enddate, or if the enddate is before the startdate. If that is true,
 	 * the reservation is possible and it will return true.
 	 * New commit to help TOKE
 	 */
 	public Controller()
 	{
 		nexus = new Nexus();	
 		boot();
 		
 	
 	}
 	
 	public void boot()
 	{
 		types = nexus.getTypes();
 		vehicles = nexus.getVehicles();
 		customers = nexus.getCostumers();
 		reservations = nexus.getReservations();
 	}
 	
 	
 	public boolean checkReservation(Reservation re)
 	{
 		
 		int o = re.getStartdate().compareTo(re.getEnddate());
 		if( o<0 )
 		{
 			/*
 			 * Remember to check if startdate the same or more than ennddate.
 			 */
 			Collection<Reservation> c = reservations.values();
 			Iterator<Reservation> i = c.iterator();
 			
 			while(i.hasNext())
 			{
 				Reservation ra = i.next();
 				Vehicle vt = ra.getVehicle();
 				if(vt.getId() == ra.getVehicle().getId()) 
 				{
 					int startValue = re.getStartdate().compareTo(ra.getEnddate());
 					int endValue = re.getEnddate().compareTo(ra.getStartdate());
 						if(startValue < 0 && endValue > 0)
 						{
 							
 							return false;
 			  
 						}
 			    	
 					}	
 				}
 		     
 			  return true;
          } 
 		
 		return false;
 	}	
 	
 	/*
 	 * This code is very VERY abstract. It start out by making an arrayList containing the cars of
 	 *  a given type. Then it runs a for-each loop that checks if there is a car that has no reservations.
 	 *  If that is not the case, it runs a for-each loop, that creates a temporary reservation and runs
 	 *  the check reservation method on it. If it is true, it will return that car. If there is no car 
 	 *  avaliable. It will return null.
 	 * 
 	 */
 	public Vehicle findCar(VehicleType v, Date start, Date end)
 	{
 		ArrayList<Vehicle> tmp = new ArrayList<Vehicle>();
 		
 		Collection<Vehicle> vehicleC = vehicles.values();
 		Iterator<Vehicle> it = vehicleC.iterator();
 		while(it.hasNext())
 			{  
 			Vehicle v1 = it.next();
 			int id = v1.getType().getId();
 			   if(id == v.getId())
 			   	{
 				   
 				   tmp.add(v1);
 			   	}   
 			}
 					
 		for(Vehicle va : tmp)
 		{
 			Customer tmpCustomer = new Customer(0,"tmp", 1, "tmp", "tmp");
 			Reservation res = new Reservation(0 ,tmpCustomer, va, start, end);
 				if(checkReservation(res)) 
 				{
 							
 					return va;
 				}
 		}
 						
 					return null;
 }
 	
 	public Vehicle searchVehicles(VehicleType v, Date start, Date end)
 	{
 		return findCar(v, start, end);
 	}
 
 	/*
 	 * This method is called by the GUI and it checks if a reservation is avaliable, and return it 
 	 * if it is. Else it resturns null.
 	 */
 	public Reservation createReservation(Customer c, VehicleType t, Date start, Date end)
 	{
 		
 		if(findCar(t, start, end) == null)
 				{
 			System.out.println("CreateReservation returns null in the findcar IF statement");
 			 		return null;
 				}
 		else {
 					Vehicle v = findCar(t, start, end);
 					Reservation r = new Reservation(0,c,v,start,end);
 					Reservation re = nexus.createEntryReservation(r);
 					if(re != null)
 					{
 					reservations.put(re.getId(), re);
 					System.out.println("Reservation seems succesful returning re in the else statement.");
 					boot();
 					return re;
 					}
 				
 			}
 		System.out.println("Returning null outside the else statement.");
 					return null;
 	}
 	
 	/*
 	 * This method creates a new customer. It checks whether the customer is already in the system. 
 	 */
 	public Customer createCustomer(String name, int phonenumber, String address, String bankaccount)
 	{
 		Customer c = new Customer(0, name, phonenumber, address, bankaccount);
 		Collection<Customer> customerC = customers.values();
 		Iterator<Customer> itt = customerC.iterator();
 			while(itt.hasNext())
 			{
 				Customer c1 = itt.next();
 				/*if(c1.getName().equals(c.getName()) && c1.getNumber() == c.getNumber())
 				
 				{
 					
 					return null;
 				}*/
 			}
 			
 				try {
 					Customer returnC = nexus.createEntryCustomer(c);
 					customers.put(returnC.getId(), returnC);
 					return returnC;
 				    }
 				catch(Exception e)
 					{
 					return null;
 					}				
 	}
 	/*
 	 * Creates a new vehicle. It check if the type is correct and already in the system.
 	 * It sends a vehicle to the database which deligates an id to it. 
 	 */
 	public Vehicle createVehicle(String make, String model, int year, VehicleType v)
 	{
 		Vehicle ve = new Vehicle(0, make, model, year, v);
 		
 		Collection<VehicleType> vehicleTypes = types.values();
 		Iterator<VehicleType> itt = vehicleTypes.iterator();
 		boolean typeExists = false;
 			while(itt.hasNext())
 			{
 				if(ve.getType().getId() == itt.next().getId())
 				{
 					typeExists = true;
 				}
 			}
 			if (!typeExists)
 			{
 				return null;
 			}
 			try {
 				Vehicle returnV = nexus.createEntryVehicle(ve);
 				vehicles.put(returnV.getId(), ve);
 				return returnV;
 				}
 			catch(Exception e) {
 				return null;
 				}
 		
 	}
 	/*
 	 * Method that creates a new vehicletype. It sends a type to Nexus, witch deligates an id to it.
 	 * It checks whether there is already a type with the same name in the system.
 	 */
 	public boolean createVehicleType(String name, double price)
 	{
 		VehicleType vee = new VehicleType(0, name, price);
 		Collection<VehicleType> c = types.values(); 
 		Iterator<VehicleType> itt = c.iterator();
 			while(itt.hasNext())
 			{
 				if(vee.getName() == itt.next().getName())
 				{
 					return false;
 				}
 				
 			}
 			try{
 				VehicleType returnT = nexus.createEntryVehicleType(vee);
 				
 				types.put(returnT.getId(), returnT);
 				return true;
 				}
 			catch(Exception e)
 			{
 				return false;
 			}
 	}
 	
 	/*
 	 * Basic accsessor methods!
 	 */
 	public Reservation getReservation(int id)
 	{
 		try 
 		{
 			if(id < reservations.size() && id>0)
 			{
 			return reservations.get(id);
 			
 			}
 			else 
 			{
 				throw new IllegalArgumentException("No reservations in that position!");
 			}
 		}
 		catch(NullPointerException e)
 		{
 			return null;
 		}
 			
 	}
 	
 	public Customer getCustomer(int id)
 	{
 		try 
 		{
 			if(id < customers.size() && id>0)
 			{
 			return customers.get(id);
 			
 			}
 			else 
 			{
 				throw new IllegalArgumentException("No reservations in that position!");
 			}
 		}
 		catch(Exception e)
 		{
 			return null;
 		}
 	}
 	
 	public Vehicle getVehicle(int id)
 	{
 		try 
 		{
 			if(id < vehicles.size() && id>0)
 			{
 			return vehicles.get(id);
 			
 			}
 			else 
 			{
 				throw new IllegalArgumentException("No reservations in that position!");
 			}
 		}
 		catch(Exception e)
 		{
 			return null;
 		}
 	}
 	
 	public VehicleType getType(int id)
 
 	{
 		try 
 		{
 			if(id < types.size() && id>0)
 			{
 			return types.get(id);
 			
 			}
 			else 
 			{
 				throw new IllegalArgumentException("No reservations in that position!");
 			}
 		}
 		catch(Exception e)
 		{
 			return null;
 		}
 	}
 	
 	public boolean deleteReservation(Reservation r)
 	{
 		Reservation rescheck = reservations.remove(r.getId());
		if(rescheck != null)
			return nexus.deleteReservation(rescheck);
		else
 			return false;
 	}
 	public boolean deleteCustomer(Customer c)
 	{
 		Customer cuscheck = customers.remove(c.getId());
		if(cuscheck != null)
			return nexus.deleteCustomer(cuscheck);
		else
 			return false;
 	}
 	public boolean deleteReservation(Vehicle v)
 	{
 		Vehicle vcheck = vehicles.remove(v.getId());
		if(vcheck != null)
			return nexus.deleteCustomer(vcheck);
		else
 			return false;
 	}
 	public boolean deleteVehicleType(VehicleType vt)
 	{
 		VehicleType vtcheck = types.remove(vt.getId());
		if(vtcheck != null)
			return nexus.deleteVehicleType(vtcheck);
		else
 			return false;
 	}
 	/*
 	 * This method takes a new reservation as a parameter and puts it in Hashmap, with a key equal to
 	 * the id of the reservation. 
 	 */
 	
 	public boolean editReservation(Reservation newR, Reservation oldR)
 	{
 		
 		if(checkReservation(newR))
 		  {
 			System.out.println("Edit: Status1");
 			int id = oldR.getId();
 			System.out.println("status 2");
 			Reservation res = new Reservation(id, newR.getCustomer(), newR.getVehicle(), new Date(newR.getStartdate()), new Date(newR.getEnddate()));
 			System.out.println("Status 3");
 			if(nexus.editReservation(res))
 			{
 			System.out.println("EDit: nexus.editR is true");
 			
 			reservations.remove(id);
 			reservations.put(id,res);
 			return true;
 			}
 			System.out.println("edit: returns false");
 			return false;
 		  }
 			System.out.println("edit: returns false2");
 		    return false;
 	}
 	
 	public boolean editVehicle(Vehicle newV, Vehicle oldV)
 	{
 		
 			int id = oldV.getId();
 			Vehicle v = new Vehicle(id, newV.getMake(), newV.getModel(), newV.getYear(), newV.getType());
 			if(nexus.editVehicle(v))
 			{
 			vehicles.remove(id);
 			vehicles.put(id,v);
 			return true;
 			}
 			return false;
 		 
 	}
 	public boolean editVehicleType(VehicleType vtnew, VehicleType vtold)
 	{
 			int id = vtold.getId();
 			VehicleType vt = new VehicleType(id, vtnew.getName(), vtnew.getPrice());
 			if(nexus.editVehicleType(vt))
 			{
 				types.remove(id);
 				types.put(id,vt);
 				return true;
 			}
 			return false;
 	}
 	
 	/*
 	 * Acssesor methods for the HashMaps.
 	 */
 	public HashMap<Integer, Reservation> getReservations()
 	{
 		return reservations;
 	}
 	
 	public HashMap<Integer, Customer> getCustomers()
 	{
 		return customers;
 	}
 	
 	public HashMap<Integer, Vehicle> getVehicles()
 	{
 		return vehicles;
 	}
 	
 	public HashMap<Integer, VehicleType> getTypes()
 	{
 		return types;
 	}
 	
 	public void close()
 	{
 		nexus.closeDatabase();
 	}
 	
 }
