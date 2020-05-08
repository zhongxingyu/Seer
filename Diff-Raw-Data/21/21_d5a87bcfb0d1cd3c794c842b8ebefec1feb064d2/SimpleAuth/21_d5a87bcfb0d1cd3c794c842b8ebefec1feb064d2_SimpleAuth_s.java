 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.tnavigator.auth.simple;
 
 import java.io.*;
 import java.net.URLEncoder;
 import java.security.*;
 import java.sql.*;
 import java.util.Map;
 
 import javax.mail.MessagingException;
 import javax.servlet.http.*;
 
 import org.w3c.dom.*;
 
 import om.tnavigator.*;
 import om.tnavigator.auth.*;
 import om.tnavigator.db.DatabaseAccess;
 import util.misc.*;
 import util.xml.*;
 
 /** 
  * Simple implementation of authentication that just uses extra database tables.
  * 
  * Tables
  * ------
  * simpleauth_users(username,password,[optional]email) 
  * simpleauth_groups(username,groupname) 
  * Usernames and group names must contain only alphanumerics.
  * 
  * Authentication
  * --------------
  * Authentication is stored in a cookie 'simpleauth':
  * username:passwordhash
  * The password hash is created by:
  * 1) Take user's password.
  * 2) Append current magic number (as string).
  * 3) Calculate SHA-1 hash.
  * The magic number is generated at random when the system starts up.
  * Note that I'm not certain how secure this method is, in particular it might
  * be better to change the number periodically... Also, Om is not intended
  * to be secure at any level without the use of SSL (https) connections.
  */
 public class SimpleAuth implements Authentication
 {
 	/** Database access */
 	private DatabaseAccess da;
 	
 	/** Name of cookie used for simple authentication */
 	private final static String COOKIE="simpleauth";
 	
 	/** Magic random number for hashing */
 	private String magicNumber;
 	
 	/** Template folder */
 	private File templatesFolder;
 	
 	/** From address for email */
 	private String mailFrom;
 	
 	private static final String LOGINFORM="simpleauth.login.xhtml";
 	private static final String OFFERLOGIN="simpleauth.offerlogin.xhtml";
 	
 	/**
 	 * Standard constructor. Creates database tables if they don't already exist.
 	 * @param ns Servlet
 	 * @throws SQLException If any problem creating tables
 	 */
 	public SimpleAuth(NavigatorServlet ns) throws SQLException
 	{
 		// Magic number: everything after the . in a random double.
 		magicNumber=(Math.random()+"").substring(2);
 		
 		// Remember settings from servlet
 		da=ns.getDatabaseAccess();		
 		templatesFolder=ns.getTemplatesFolder();
 		mailFrom=ns.getNavigatorConfig().getAlertMailFrom();
 		
 		// Check tables are present
 		DatabaseAccess.Transaction dat=da.newTransaction();
 		try
 		{
 			dat.query("SELECT * FROM simpleauth_users WHERE username=''");
 			return;
 		}
 		catch(SQLException se)
 		{
 		}
 		finally
 		{
 			dat.finish();
 		}
 		
 		// No tables? Let's make some
 		dat=da.newTransaction();
 		try
 		{
 			// Simple version that is supposed to work on all databases, hence no
 			// foreign key etc
 			dat.update("CREATE TABLE simpleauth_users(username VARCHAR NOT NULL PRIMARY KEY, password VARCHAR NOT NULL,email VARCHAR)");
 			dat.update("CREATE TABLE simpleauth_groups(username VARCHAR NOT NULL, groupname VARCHAR NOT NULL)");
 			dat.update("CREATE INDEX simpleauth_groups_username ON simpleauth_groups(username)");
 			return;
 		}
 		finally
 		{
 			dat.finish();
 		}		
 	}
 
 	public void becomeTestUser(HttpServletResponse response,String suffix)
 	{
 		Cookie c=new Cookie(COOKIE,"!tst"+suffix+":");
 		c.setPath("/");
 		response.addCookie(c);
 	}
 
 	public void close()
 	{
 	}
 
 	public String getLoginOfferXHTML(HttpServletRequest request) throws IOException
 	{
 		String offer=IO.loadString(new FileInputStream(new File(templatesFolder,OFFERLOGIN)));
 		return XML.replaceToken(offer,"%%","LOGINURL",XHTML.escape(getLoginPageURL(request)		,XHTML.ESCAPE_ATTRSQ));
 	}
 	
 	private String getCookie(HttpServletRequest request)
 	{
 		Cookie[] ac=request.getCookies();
 		if(ac==null) ac=new Cookie[0];
 		for(int i=0;i<ac.length;i++)
 		{
 			if(ac[i].getName().equals(COOKIE))
 			{
 				String cookie=ac[i].getValue();
 				if(cookie.indexOf(':')!=-1) return cookie;
 			}
 		}
 		return null;
 	}
 
 	public UncheckedUserDetails getUncheckedUserDetails(HttpServletRequest request)
 	{
 		String cookie=getCookie(request);
 		if(cookie==null)
 			return new SimpleUncheckedUser(null,null);
 		else
 			return new SimpleUncheckedUser(cookie,cookie.substring(0,cookie.indexOf(':')));
 	}
 	
 	private String hash(String password) throws IOException
 	{
 		String plaintext=password+magicNumber;
     MessageDigest md=null;
     try
     {
       md=MessageDigest.getInstance("SHA");
     }
     catch(NoSuchAlgorithmException e)
     {
       throw new IOException("Encryption system (java.security) not available: "+e.getMessage());
     }
     try
     {
       md.update(plaintext.getBytes("UTF-8"));
     }
     catch(UnsupportedEncodingException e)
     {
       throw new Error(e);
     }
 
     byte hash[]=md.digest();
     StringBuffer hex=new StringBuffer();
     for(int i=0;i<hash.length;i++)
     {
    		int n=((int)hash[i])+128; // 0-255
     		if(n<16) hex.append("0");
   			hex.append(Integer.toHexString(n));
     }
     return hex.toString();
 	}
 
 	public UserDetails getUserDetails(HttpServletRequest request,
 		HttpServletResponse response,boolean bRequireLogin) throws IOException
 	{
 		SimpleUncheckedUser details=(SimpleUncheckedUser)getUncheckedUserDetails(request);
 		if(details.getCookie()!=null) 
 		{
 			// Find out password for purported user
 			DatabaseAccess.Transaction dat=null;
 			try
 			{
 				dat=da.newTransaction();
 				ResultSet rs=dat.query("SELECT password,email FROM simpleauth_users WHERE username="+
 					Strings.sqlQuote(details.getUsername()));
 				if(rs.next())
 				{
 					String password=rs.getString(1),email=rs.getString(2);
 					if(details.getCookie().equals(details.getUsername()+":"+					hash(password)))
 					{
 						SimpleUser authenticated=new SimpleUser(details.getCookie(),details.getUsername(),email);
 						rs=dat.query("SELECT groupname FROM simpleauth_groups WHERE username="+
 							Strings.sqlQuote(details.getUsername()));
 						while(rs.next())
 						{
 							authenticated.addGroup(rs.getString(1));
 						}
 						return authenticated;
 					}
 				}
 			}
 			catch(SQLException se)
 			{
 				throw new IOException("Error authenticating user in database: "+se.getMessage());
 			}
 			finally
 			{
 				if(dat!=null) dat.finish();
 			}
 			// Clear invalid cookie
 			Cookie c=new Cookie(COOKIE,"");
 			c.setMaxAge(0);
 			c.setPath("/");
 			response.addCookie(c);			
 		} 
 		// No cookie or auth failed
 		if(bRequireLogin) 
 		{
 			redirect(request,response);
 			return null;
 		}
 		else
 		{
 			return SimpleUser.NOTLOGGEDIN;
 		}
 	}
 
 	public boolean handleRequest(String subPath,HttpServletRequest request,
 		HttpServletResponse response) throws Exception
 	{
 		// Only offer login page
 		if(!subPath.equals("login")) return false;
 		
 		// Load form and fill in URL
 		Document d=XML.parse(new File(templatesFolder,LOGINFORM));
 		Element urlField=XML.find(d,"name","url");
 		String url=request.getParameter("url");
 		if(url==null) throw new Exception("Missing url parameter");
 		urlField.setAttribute("value",url);
 		
 		if(request.getMethod().equals("POST"))
 		{
 			// Get username/password
 			String 
 				username=request.getParameter("username"),
 				password=request.getParameter("password");
 			
 			// Check they match
 			DatabaseAccess.Transaction dat=da.newTransaction();
 			try
 			{
 				ResultSet rs=dat.query("SELECT COUNT(*) FROM simpleauth_users WHERE username="+
 					Strings.sqlQuote(username)+" AND password="+Strings.sqlQuote(password));
 				rs.next();
 				if(rs.getInt(1)>0)
 				{
 					// OK! Set cookie and redirect
 					Cookie c=new Cookie(COOKIE,username+":"+hash(password));
 					c.setPath("/");
 					response.addCookie(c);
 					response.sendRedirect(url);
 				}
 				else
 				{
 					// Not OK, try again
 					Element errorText=XML.find(d,"class","warning");
 					XML.createText(errorText,"Username or password not recognised. " +
 						"Please try again, or contact the administrator for help.");
 					XHTML.output(d,request,response,"en");
 				}
 			}
 			finally
 			{
 				dat.finish();
 			}			
 		}
 		else
 		{
 			XHTML.output(d,request,response,"en");
 		}
 		
 		return true;		
 	}
 
 	public void obtainPerformanceInfo(Map m)
 	{
 		// None tracked.
 	}
 
 	public void redirect(HttpServletRequest request,HttpServletResponse response)
 		throws IOException
 	{
 		response.sendRedirect(getLoginPageURL(request));
 	}
 	
 	private String getLoginPageURL(HttpServletRequest request) throws IOException
 	{
 		// Get URL up to before webapp context, add the context back (doesn't end
 		// in /) then add in the path to login page
 		String requestURL=request.getRequestURI();
 		String redirectURL=requestURL.substring(0,
 			requestURL.indexOf(request.getContextPath()))+request.getContextPath()+
 			"/!auth/login";
 		return redirectURL+"?url="+URLEncoder.encode(requestURL,"UTF-8");
 	}
 
 	public String sendMail(String username,String personID,String content,
 		int emailType) throws IOException
 	{
 		// Split into subject and message
 		String[] subjectAndMessage=content.split("\n",2);
 		if(subjectAndMessage.length!=2) throw new IOException(
 			"Email content must begin with a one-line subject then contain additional content"); 
 		
 		// Find out password for purported user
 		DatabaseAccess.Transaction dat=null;
 		try
 		{
 			dat=da.newTransaction();
 			ResultSet rs=dat.query("SELECT email FROM simpleauth_users WHERE username="+
 				Strings.sqlQuote(username));
 			if(rs.next())
 			{
 				String address=rs.getString(1);
 				Mail.send(mailFrom,mailFrom,new String[] {address},null,subjectAndMessage[0],
 					subjectAndMessage[1],null);
 				return "";
 			}
 			else
 			{
 				throw new IOException("Can't find email address for user");
 			}
 		}
 		catch(SQLException se)
 		{
 			throw new IOException("Error looking up user email address: "+se.getMessage());
 		}
 		catch(MessagingException me)
 		{
 			IOException e=new IOException("Error sending user email");
 			e.initCause(me);
 			throw e;
 		}
 		finally
 		{
 			if(dat!=null) dat.finish();
 		}
 		
 	}
 }
