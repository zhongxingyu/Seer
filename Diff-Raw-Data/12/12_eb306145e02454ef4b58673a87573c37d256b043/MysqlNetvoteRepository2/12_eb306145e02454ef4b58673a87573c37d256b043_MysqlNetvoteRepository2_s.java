 package net.buhacoff.netvote.server;
 
 import java.security.*;
 import java.security.spec.InvalidKeySpecException;
 import javax.crypto.*;
 import javax.crypto.spec.SecretKeySpec;
 import sun.misc.*;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import javax.sql.DataSource;
 import net.buhacoff.netvote.model.*;
 import org.apache.commons.codec.binary.Base64;
 
 
 public class MysqlNetvoteRepository2 implements NetvoteRepository 
 {
     private DataSource dataSource = null;
 	private Connection con = null;
 	private Statement st = null;
 	private ResultSet rs = null;
 	private static final String ALGO = "AES";
     private static byte[] keyValue;  //= new byte[] { 'N','3','3','7','v','0','T','e','E','7','o','V','T','e','E','n' };
 	
 	
     public void setDataSource(DataSource ds) 
 	{
         dataSource = ds;
     }
 
     public void setEncryptionKey(byte[] encKey) {
         keyValue = encKey;
     }
 	
     public void open()
 	{
 		try
 		{
 			// create the db connection
			if (con == null) { con = dataSource.getConnection(); }
 		}
 		catch (Exception e)
 		{
 			System.err.println("Exception: " + e.getMessage());
 		}
 	}
 
 	
     public void close()
 	{
 		try
 		{
 			// close db management variables
 			if (rs != null) { rs.close(); }
 			if (st != null) { st.close(); }
 			if (con != null) { con.close(); }
 		}
 		catch (Exception e)
 		{
 			System.err.println("Exception: " + e.getMessage());
 		}
     }
 
 	
     public IssueList getIssueList() 
 	{
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
 	
 	// represents regvote, register a voter
     public String addVoter(VoterPrivateRecord voter, VoterSecretPassphrase secret)
 	{
         String retVal = "FAILED";
 		String query = "";
 		
 		try
 		{
 			st = con.createStatement();
 			String encFName = encrypt(voter.firstName);
 			String encLName = encrypt(voter.lastName);
 			String encAddress = encrypt(voter.address);
 			
 			// first verify voter isn't already registered
 			if (verifyVoter(voter.voterId) == 0)
 			{
 				int success = 0;
 				
 				// query to add to database
 				query = "INSERT INTO Voters (ssn, first_name, last_name, address, passphrase) VALUES ('"+voter.voterId+"', '"+encFName+"', '"+encLName+"', '"+encAddress+"', '"+secret.passphrase+"')";
 
 				success = st.executeUpdate(query);
 				
 				if (success == 1) 
 				{ 
 					System.out.println("Voter Registered Successfully"); 
 					retVal = "SUCCESS";
 				}
 				else 
 				{ 
 					// problem registering
 					System.out.println("Couldn't register voter");
 					retVal += " voter couldn't be registered";
 				}
 				
 			}
 			else if (verifyVoter(voter.voterId) == 1)
 			{
 				System.out.println("This voter has already registered");
 				retVal += " voter has already registered";
 			}
 			else
 			{
 				// error: unknown
 				System.out.println("Unknown error");
 				retVal += " unknown";
 			}
 			
 			return retVal;
 		}
 		catch (Exception e)
 		{
 			System.err.println("Exception: " + e.getMessage());
 			return retVal;
 		}		
     }
 	
 	
 	// retrieve a field from voter (must know SSN to get the field)
 	public String getVoterInfo(String voterSSN, String field)
 	{
 		
 		String retVal = "";
 		int success = 0;
 		
 		try
 		{
 			// query to select from database
 			String query = "SELECT "+field+" FROM Voters WHERE ssn='"+voterSSN+"'";
 			rs = st.executeQuery(query);
 		
 			if (rs.next())
 			{
 				retVal = decrypt(rs.getString(1));
 			}
 			else 
 			{
 				retVal = "FAILED";
 			}
 		
 			return retVal;
 		}
 		catch (Exception e)
 		{
 			System.err.println("Exception: " + e.getMessage());
 			
 			return "FAILED";
 		}
 	}
 	
 	
 	// represents "setvote", count a ballot
     public String countVote(Ballot ballot)
 	{
 		String retVal = "FAILED";
 		String query = "";
 		
 		try
 		{
 			st = con.createStatement();
 			
 			// first verify the voter is eligible to vote
 			if (verifyVoter(ballot.voterId) == 1)
 			{
 				int success = 1; //check this before adding additional ballot info
 				
 				if (ballot.anonymous == true) //anonymous voting, encrypt voterID
 				{
 					String encSSN = encrypt(ballot.voterId);
 					
 					query = "SELECT * FROM Issues WHERE type='"+ballot.issueId+"' AND ssn='"+encSSN+"'";
 					rs = st.executeQuery(query);
 					
 					// hasn't voted on this issue yet, add choice
 					if (!rs.next()) 
 					{
 						// ssn is 'anon'; can search database on that value
 						query = "INSERT INTO Issues (type, choice, ssn) VALUES ('"+ballot.issueId+"', '"+ballot.choice+"', '"+encSSN+"')";
 						success = st.executeUpdate(query);
 						
 						if (success == 1) { retVal = "SUCCESS"; }
 					}
 					else
 					{
 						System.out.println("Voter has already voted on this issue");
 						retVal += " already voted on this issue";
 					}
 				}
 				else
 				{
 					query = "SELECT * FROM Issues WHERE type='"+ballot.issueId+"' AND ssn='"+ballot.voterId+"'";
 					rs = st.executeQuery(query);
 					
 					// hasn't voted on this issue yet, add choice
 					if (!rs.next()) 
 					{
 						query = "INSERT INTO Issues (type, choice, ssn) VALUES ('"+ballot.issueId+"', '"+ballot.choice+"', '"+ballot.voterId+"')";
 						success = st.executeUpdate(query);
 						
 						// add verification sql
 						
 						if (success == 1) { retVal = "SUCCESS"; }
 					}
 					else
 					{
 						System.out.println("Voter has already voted on this issue");
 						retVal += " already voted on this issue";
 					}
 				}
 			}
 			else if (verifyVoter(ballot.voterId) == 0)
 			{
 				System.out.println("This voter has not yet registered");
 				retVal += " voter hasn't registered";
 			}
 			else
 			{
 				System.out.println("Unknown error");
 				retVal += " unknown";
 			}
 			
 			return retVal;
 		}
 		catch (Exception e)
 		{
 			System.err.println("Exception: " + e.getMessage());
 			return retVal;
 		}
 	}
 	
 	
 	// represents "getvote" and "getpercent", return the vote tally/perent of a given issue
 	public IssueStatus issueStatus(String issueId)
 	{
 		IssueStatus is = new IssueStatus(); //what we are returning
 		
 		try
 		{
 			st = con.createStatement();
 			
 			
 			String toOut = ""; //what we are printing (for testing)
 			int numOptions = 0; //number of options for the issue type
 			
 			toOut += "---- "+issueId+" Tally ----<br>";
 			
 			// query to find numOptions
 			String query = "SELECT COUNT(*) FROM (SELECT DISTINCT choice FROM Issues WHERE type='"+issueId+"') I1";
 			rs = st.executeQuery(query);
 			
 			if (rs.next())
 			{
 				numOptions = Integer.parseInt(rs.getString(1)); //get numOptions from return on query
 				String[] options = new String[numOptions]; //create array to store the options
 				int counter = 0;
 				
 				query = "SELECT DISTINCT choice FROM Issues WHERE type='"+issueId+"'";
 				rs = st.executeQuery(query);
 				
 				while (rs.next())
 				{
 					if (counter >= numOptions) { break; } //safety
 					
 					// store each option in the array
 					options[counter] = rs.getString(1);
 					counter++;
 				}
 				
 				// count the number for each option and add to output
 				for (int i=0; i<numOptions; i++) 
 				{ 
 					toOut += options[i]+": ";
 					
 					query = "SELECT COUNT(*) FROM Issues WHERE type='"+issueId+"' AND choice='"+options[i]+"'";
 					rs = st.executeQuery(query);
 					
 					if (rs.next())
 					{
 						toOut += rs.getString(1)+" votes<br>";
 						
 						is.issueTally.put(options[i], Integer.parseInt(rs.getString(1)));
 					}
 				}
 			}
 			
 			System.out.println(toOut + "\n"); //show the tally results
 			
 			
 			toOut = "";
 			
 			// query to find the number of options
 			query = "SELECT COUNT(*) FROM (SELECT DISTINCT choice FROM Issues WHERE type='"+issueId+"') I1";
 			rs = st.executeQuery(query);
 			
 			if (rs.next())
 			{
 				numOptions = Integer.parseInt(rs.getString(1));
 				String[] options = new String[numOptions];
 				
 				// variables to calculate the percentage of each option
 				int counter = 0;
 				int totalVotes = 0;
 				double percent = 0;
 				
 				query = "SELECT DISTINCT choice FROM Issues WHERE type='"+issueId+"'";
 				rs = st.executeQuery(query);
 				
 				// store each option in array
 				while (rs.next())
 				{
 					if (counter >= numOptions) { break; }
 					
 					options[counter] = rs.getString(1);
 					counter++;
 				}
 				
 				// query to find total number of votes for given issue type
 				query = "SELECT COUNT(*) FROM Issues WHERE type='"+issueId+"'";
 				rs = st.executeQuery(query);
 				
 				if (rs.next()) { totalVotes = Integer.parseInt(rs.getString(1)); }
 				
 				// loop through each option, append percent from total that each
 				//  has attained to the output
 				for (int i=0; i<numOptions; i++) 
 				{ 
 					toOut += options[i]+": ";
 					
 					query = "SELECT COUNT(*) FROM Issues WHERE type='"+issueId+"' AND choice='"+options[i]+"'";
 					rs = st.executeQuery(query);
 					
 					if (rs.next())
 					{
 						percent = ((double)Integer.parseInt(rs.getString(1))/(double)totalVotes)*100;
 						toOut += percent+" percent<br>";
 						
 						is.issuePercent.put(options[i], percent);
 					}
 				}
 			}
 			
 			System.out.println(toOut); //show the percent results
 			
 			return is; //return the IssueStatus object
 		}
 		catch (Exception e)
 		{
 			System.out.println("Exception: " + e.getMessage());
 			
 			return is;
 		}
 	}
 	
 	
 	// helper method, verify a voter's registration status / vote status
 	public int verifyVoter(String SSN)
 	{
 		int retVal = -1; //represents unknown error
 		
 		try
 		{
 			// query to find the voter
 			String query = "SELECT * FROM Voters WHERE ssn='"+SSN+"'";
 			rs = st.executeQuery(query);
 			
 			// found the voter, check vote status
 			if (rs.next()) 
 			{ 
 				retVal = 1; //has registered
 			}
 			else
 			{
 				retVal = 0; //hasn't registered
 			}
 			
 			return retVal;
 		}
 		catch (Exception e)
 		{
 			System.err.println("Exception: " + e.getMessage());
 			return retVal;
 		}
 	}
 	
 	
     public VoterSecretPassphrase getVoterSecretPassphrase(Voter voter) throws IOException {
         try {
             st = con.createStatement();
             String query = "SELECT passphrase FROM Voters WHERE ssn = '"+voter.voterId+"'";  // XXX TODO all of the queries in this class need to be protected with prepared statements
             rs = st.executeQuery(query);
             if(rs.next()) {
                 String passphrase = rs.getString("passphrase");
                 VoterSecretPassphrase secret = new VoterSecretPassphrase();
                 secret.registrationAuthorityId = voter.registrationAuthorityId;
                 secret.voterId = voter.voterId;
                 secret.passphrase = passphrase;
                 return secret;
             }
             else {
                 return null;
             }
         }
         catch(Exception e) {
             throw new IOException(e);
         }
         finally {
             try {
                rs.close();
                st.close();            
             }
             catch(Exception e) {
                 System.err.println("Failed to close mysql statement: "+e.toString());
             }
         }
     }
 	
 	
 	// aes encryption function
 	public static String encrypt(String Data) throws Exception 
 	{
         Key key = generateKey();
         Cipher c = Cipher.getInstance(ALGO);
         c.init(Cipher.ENCRYPT_MODE, key);
         byte[] encVal = c.doFinal(Data.getBytes());
         String encryptedValue = Base64.encodeBase64String(encVal);
         return encryptedValue;
     }
 	
 	
 	// aes decryption function
     public static String decrypt(String encryptedData) throws Exception 
 	{
         Key key = generateKey();
         Cipher c = Cipher.getInstance(ALGO);
         c.init(Cipher.DECRYPT_MODE, key);
         byte[] decordedValue = Base64.decodeBase64(encryptedData);
         byte[] decValue = c.doFinal(decordedValue);
         String decryptedValue = new String(decValue);
         return decryptedValue;
     }
 	
 	
 	// generates the key given the "keyValue" above
     private static Key generateKey() throws Exception 
 	{
         Key key = new SecretKeySpec(keyValue, ALGO);
         return key;
 	}
 
     public boolean isVoterRegistered(Voter voter) throws IOException {
         String result = getVoterInfo(voter.voterId, "ssn");
         if( result != null && !result.equals("FAILED") ) { return true; }
         return false;
     }
 
     public boolean containsBallot(Ballot ballot) throws IOException {
         boolean exists = false;
         String query = "SELECT * FROM Issues WHERE type='"+ballot.issueId+"' AND ssn='"+ballot.voterId+"'";
         try {
             st = con.createStatement();
             rs = st.executeQuery(query);
             if( rs.next() ) {
                 exists = true;
             }
            rs.close();
            st.close();
         }
         catch(Exception e) {
             e.printStackTrace(System.err);
         }
         return exists;
     }
 
 }
