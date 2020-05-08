 package com.brotherlogic.beer.servlets;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.brotherlogic.beer.Checker;
 import com.brotherlogic.beer.actions.foursqure.FourSquareVenue;
 import com.brotherlogic.beer.actions.foursqure.FoursquareVenueFinder;
 import com.brotherlogic.beer.actions.untappd.Beer;
 import com.brotherlogic.beer.db.AssetManager;
 import com.brotherlogic.beer.db.BeerOff;
 import com.brotherlogic.beer.db.Drink;
 import com.brotherlogic.beer.db.User;
 import com.brotherlogic.beer.db.Venue;
 import com.google.gson.JsonParser;
 
 public class WebInterface extends HttpServlet
 {
   String baseURL = "http://untappdpicker.herokuapp.com/";
    String COOKIE_NAME = "untappdpicker_cookie";
    String redirectURL = "http://untappdpicker.herokuapp.com/";
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
          IOException
    {
       // Get the user name from the cookie
       String token = getUserToken(req);
       System.out.println("USER TOKEN = " + token);
 
       if (req.getParameter("action") != null && req.getParameter("action").equals("getBeers"))
       {
          Checker c = new Checker();
          List<Beer> beers = new LinkedList<Beer>();
          Venue v = null;
 
          if (req.getParameter("fsv") != null)
             v = AssetManager.getInstance(token).getVenue(req.getParameter("fsv"));
          else
             v = AssetManager.getInstance(token).getVenue(Integer.parseInt(req.getParameter("v")));
 
          beers.addAll(c.getOptions(v, AssetManager.getInstance(token).getUser("BrotherLogic")));
 
          String retString = "<HTML><BODY>";
          int count = 1;
          for (Beer b : beers)
             retString += "<p>" + (count++) + ": " + b.getName() + " ("
                   + b.getBeerScore(token).getScore() + ")" + "  <A HREF='Location?action=off&bid="
                   + b.getId() + "&vid=" + v.getId() + "'>Beer is off</A></p>";
          retString += "</BODY></HTML>";
 
          PrintStream ps = new PrintStream(resp.getOutputStream());
          ps.println(retString);
          ps.close();
       }
       else if (req.getParameter("action") != null
             && req.getParameter("action").equals("resolveloc"))
       {
          double lat = Double.parseDouble(req.getParameter("lat"));
          double lon = Double.parseDouble(req.getParameter("lon"));
 
          FoursquareVenueFinder vFinder = new FoursquareVenueFinder();
          List<FourSquareVenue> venues = vFinder.locateClosest(lat, lon);
          String retString = "<HTML>\n<BODY>";
          for (FourSquareVenue venue : venues)
             retString += "<p><A href='Location?action=getBeers&fsv=" + venue.getId() + "'>"
                   + venue.getName() + "</A></p>";
          retString += "</BODY>";
          retString += "</HTML>";
 
          PrintStream ps = new PrintStream(resp.getOutputStream());
          ps.println(retString);
          ps.close();
       }
       else if (req.getParameter("code") != null)
       {
          String code = req.getParameter("code");
          String url = "https://untappd.com/oauth/authorize/?client_id=" + System.getenv("UT_ID")
                + "&client_secret=" + System.getenv("UT_SECRET")
                + "&response_type=code&redirect_url=" + redirectURL + "&code=" + code;
          String response = "";
 
          System.out.println("Going for " + url);
          BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(url).openStream()));
          for (String line = reader.readLine(); line != null; line = reader.readLine())
             response += line;
 
          System.out.println("READ = " + response);
          JsonParser parser = new JsonParser();
          String nToken = parser.parse(response).getAsJsonObject().get("response").getAsJsonObject()
                .get("access_token").getAsString();
          System.out.println("TOKEN = " + nToken);
 
          Cookie cookie = new Cookie(COOKIE_NAME, nToken);
          cookie.setMaxAge(60 * 60 * 24 * 365);
          cookie.setPath("/");
          resp.addCookie(cookie);
 
          System.out.println("Added cookie");
 
          // String result =
          // "<HTML><BODY><SCRIPT>window.location.href='/'</SCRIPT></BODY></HTML>";
          String result = "";
          PrintStream ps = new PrintStream(resp.getOutputStream());
          ps.println(result);
          ps.close();
       }
       else if (req.getParameter("action") != null && req.getParameter("action").equals("off"))
       {
          int beerId = Integer.parseInt(req.getParameter("bid"));
          int venueId = Integer.parseInt(req.getParameter("vid"));
 
          BeerOff off = new BeerOff(venueId, beerId, System.currentTimeMillis());
          AssetManager.getInstance(token).beerOff(off);
 
          String result = "<HTML><BODY><SCRIPT>window.location.href='Location?action=getBeers&v="
                + venueId + "'</SCRIPT></BODY></HTML>";
          PrintStream ps = new PrintStream(resp.getOutputStream());
          ps.println(result);
          ps.close();
       }
       else if (req.getParameter("action") != null && req.getParameter("action").equals("callback"))
       {
          String result = "Hello";
          PrintStream ps = new PrintStream(resp.getOutputStream());
          ps.println(result);
          ps.close();
       }
       else if (req.getParameter("action") != null && req.getParameter("action").equals("login"))
       {
          if (token != null && !token.equals("not_logged_in"))
          {
             String result = "<HTML><BODY><SCRIPT>window.location.href='" + baseURL
                   + "locateme'</SCRIPT></BODY></HTML";
 
             PrintStream ps = new PrintStream(resp.getOutputStream());
             ps.println(result);
             ps.close();
          }
          else
          {
 
             String result = "<HTML><BODY><SCRIPT>window.location.href='http://untappd.com/oauth/authenticate/?client_id="
                   + System.getenv("UT_ID")
                   + "&client_secret="
                   + System.getenv("UT_SECRET")
                   + "&response_type=code&redirect_url=" + redirectURL + "'</SCRIPT></BODY></HTML>";
 
             PrintStream ps = new PrintStream(resp.getOutputStream());
             ps.println(result);
             ps.close();
          }
       }
       else if (req.getParameter("action") != null && req.getParameter("action").equals("resolve"))
       {
          String result = "<HTML><BODY><SCRIPT>"
                + "navigator.geolocation.getCurrentPosition(locationSuccess, locationFail);\n"
                + "function locationSuccess(position)\n{"
                + "   latitude = position.coords.latitude;\n"
                + "   longitude = position.coords.longitude;\n"
                + "   window.location.href='Location?action=resolveloc&lat=' + latitude + '&lon=' + longitude;\n"
                + "}" + "" + "function locationFail() {" + "alert('Oops, could not find you.');"
                + "}</SCRIPT></BODY></HTML>";
          PrintStream ps = new PrintStream(resp.getOutputStream());
          ps.println(result);
          ps.close();
       }
       else if (req.getParameter("action") != null && req.getParameter("action").equals("manadd"))
       {
          AssetManager manager = AssetManager.getInstance(getUserToken(req));
 
          int venueid = Integer.parseInt(req.getParameter("vid"));
          int beerid = Integer.parseInt(req.getParameter("bid"));
 
          Drink d = new Drink(new Beer(beerid, "MAN"), System.currentTimeMillis(), new User(
                "ManualUser"), venueid);
          manager.addDrink(d);
       }
       else
       {
          System.err.println("ERROR:");
          for (Object e : req.getParameterMap().entrySet())
             System.err.println(((Entry) e).getKey());
       }
    }
 
    private String getUserToken(HttpServletRequest req)
    {
       if (req.getCookies() != null)
          for (Cookie cookie : req.getCookies())
          {
             System.out.println("COOKIE = " + cookie.getName());
             if (cookie.getName().equals(COOKIE_NAME))
             {
                AssetManager.setToken(cookie.getValue());
                return cookie.getValue();
             }
             else
                System.out.println("Skipping");
          }
       else
          System.out.println("NO COOKIES?");
       // System.exit(1);
       return "not_logged_in";
    }
 }
