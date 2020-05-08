 package dk.wallviz.skylinehotels;
 
 import java.io.*;
 import javax.servlet.http.*;
 import java.util.*;
 
 @SuppressWarnings("serial")
 public class Skyline_hotelsServlet extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		// retrieving hotel data
 		Hotel[] hotels = getData();
 		//resp.getWriter().println(hotels.length+" hotels read<br/>");
 		// filtering - not efficient, but simple code
 		HotelPredicate predicate = buildFilter(req);
 		hotels = filter(hotels,predicate);
 		//resp.getWriter().println(hotels.length+" hotels filtered<br/>");
 		resp.setContentType("application/json");
 		resp.getWriter().println(toJSON(hotels));
 	}
 	
 	private Hotel[] getData() throws IOException {
 		/*	DATA:
 		* 	0 order	id	name	address1	city
 		*	5 postalCode	propertyCategory	hotelRating	confidenceRating	tripAdvisorRating
 		*	10 highRate	lat	long	proximityDistance	Business Center
 		*   15 Fitness Center	Hot Tub On-site	Internet Access Available	Kids Activities	Kitchen or Kitchenette	
 		*   20 Pets Allowed	Pool	Restaurant On-site	Spa On-site	Whirlpool Bath Available
 		*   25 Breakfast	Babysitting	Jacuzzi	Parking	Room Service
 		*   30 Accessible Path of Travel	Accessible Bathroom	Roll-in Shower	Handicapped Parking	In-room Accessibility	Accessibility Equipment for the Deaf	
 		*   35 Braille or Raised Signage	Free Airport Shuttle	Indoor Pool	Outdoor Pool	Extended Parking	
 		*   39 Free Parking	DistFromColosseum	DistFromTreviFountain	picture
 		*   OBJECT:
 		*    id,  name,  address1,  city,
 			 postalCode,  propertyCategory,  hotelRating,
 			 tripAdvisorRating,  highRate,  lat,  lon,
 			 proximityDistance,  internet,  pool,
 			 distFromColosseum,  distFromTreviFountain,
 			 picture
 		*/
 		LineNumberReader in = new LineNumberReader(new FileReader("assets/data/hotel_data.tsv"));
 		in.readLine(); // skip header
 		String hotel;
 		ArrayList<Hotel> list = new ArrayList<Hotel>();
 		while ((hotel=in.readLine())!=null) {
 			String[] r = hotel.split("\t");
 			Hotel h = new Hotel(r[1], // id
 					r[2], // name
 					r[3], // address1
 					r[4], // city
 					r[5], // postalCode
 					Integer.parseInt(r[6]), // propertyCategory
 					d(r[7]), // hotelRating
 					d(r[9]), // tripAdvisorRating
 					d(r[10]), // highRate
 					d(r[11]), // lat
 					d(r[12]), // long
 					d(r[13]), // proximityDistance
 					r[17].equals("Y"), // internet
 					r[38].equals("Y"), // pool
 					d(r[42]), // distFromColosseum 
 					d(r[43]), // distFromTreviFountain
 					r[44] // picture
 					);
 			list.add(h);
 		}
		in.close();
 		return list.toArray(new Hotel[0]);
 	}
 	
 	HotelPredicate buildFilter(HttpServletRequest req) {
 		HotelPredicate pred = new HotelPredicate();
 		String val;
 		if ((val = req.getParameter("propertyCategoryStart"))!=null)
 			pred.propertyCategoryStart = Integer.parseInt(val);
 		if ((val = req.getParameter("propertyCategoryEnd"))!=null)
 			pred.propertyCategoryEnd = Integer.parseInt(val);
 		if ((val = req.getParameter("hotelRatingStart"))!=null)
 			pred.hotelRatingStart = Double.parseDouble(val);
 		if ((val = req.getParameter("hotelRatingEnd"))!=null)
 			pred.hotelRatingEnd = Double.parseDouble(val);
 		if ((val = req.getParameter("tripAdvisorRatingStart"))!=null)
 			pred.tripAdvisorRatingStart = Double.parseDouble(val);
 		if ((val = req.getParameter("tripAdvisorRatingEnd"))!=null)
 			pred.tripAdvisorRatingEnd = Double.parseDouble(val);
 		if ((val = req.getParameter("highRateStart"))!=null)
 			pred.highRateStart = Double.parseDouble(val);
 		if ((val = req.getParameter("highRateEnd"))!=null)
 			pred.highRateEnd = Double.parseDouble(val);
 		if ((val = req.getParameter("proximityDistanceStart"))!=null)
 			pred.proximityDistanceStart = Double.parseDouble(val);
 		if ((val = req.getParameter("proximityDistanceEnd"))!=null)
 			pred.proximityDistanceEnd = Double.parseDouble(val);
 		if ((val = req.getParameter("distFromColosseumStart"))!=null)
 			pred.distFromColosseumStart = Double.parseDouble(val);
 		if ((val = req.getParameter("distFromColosseumEnd"))!=null)
 			pred.distFromColosseumEnd = Double.parseDouble(val);
 		if ((val = req.getParameter("distFromTreviFountainStart"))!=null)
 			pred.distFromTreviFountainStart = Double.parseDouble(val);
 		if ((val = req.getParameter("distFromTreviFountainEnd"))!=null)
 			pred.distFromTreviFountainEnd = Double.parseDouble(val);
 		if ((val = req.getParameter("internet"))!=null)
 			pred.internet = Boolean.parseBoolean(val);
 		if ((val = req.getParameter("pool"))!=null)
 			pred.pool = Boolean.parseBoolean(val);
 		return pred;
 	}
 	
 	private Hotel[] filter(Hotel[] hotels, HotelPredicate pred) {
 		ArrayList<Hotel> list = new ArrayList<Hotel>();
 		for (Hotel h: hotels)
 			if (pred.check(h))
 				list.add(h);
 		return list.toArray(new Hotel[0]);
 	}
 	
 	private String toJSON(Hotel[] hotels) {
 		StringBuffer buf = new StringBuffer("[");
 		for (int i = 0; i<hotels.length; i++) {
 			buf.append(hotels[i].toJSON(i));
 			if (i!=hotels.length-1) buf.append(",");
 		}
 		buf.append("]");
 		return buf.toString();
 	}
 	
 	private double d(String s) {
 		if (s.equals("")) return -1;
 		return Double.parseDouble(s);
 	}
 }
