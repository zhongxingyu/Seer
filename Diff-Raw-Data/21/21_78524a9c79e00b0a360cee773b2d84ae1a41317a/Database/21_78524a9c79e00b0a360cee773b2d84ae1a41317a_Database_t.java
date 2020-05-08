 package Database;
 
 import fields.Form;
 import fields.response.FormResponse;
 
 import java.io.*;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Scanner;
 /**
  * Created with IntelliJ IDEA.
  * User: ahanes
  * Date: 6/23/13
  * Time: 3:24 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Database {
     private Connection conn;
 
     public Database(String fName) throws SQLException, FileNotFoundException {
         Scanner in = new Scanner(new File(fName));
         conn = DriverManager.getConnection(
                 "jdbc:postgresql://"+in.nextLine()+"/" + in.nextLine(), in.nextLine(), in.nextLine());
         conn.setAutoCommit(false);
     }
 
     public Database(String hostname, String username, String password, String database) throws SQLException,
             FileNotFoundException {
             conn = DriverManager.getConnection(
                     "jdbc:postgresql://"+hostname+"/" + database, username, password); //For travis-ci
             conn.setAutoCommit(false);
     }
    //For testing
     public void initTables() throws SQLException, IOException {
         Scanner s = new Scanner(new File("schema.psql"));
         String str = "";
         while(s.hasNext()) {
             str += s.next() + " ";
         }
         PreparedStatement st = conn.prepareStatement(str);
         st.executeUpdate();
         conn.commit();
     }
    //For testing
    public void cleanTables() throws SQLException {
        PreparedStatement st = conn.prepareStatement("drop table form, formresponse, validvoter, votinguser");
        st.executeUpdate();
        conn.commit();
    }
 
     public void addVotingUser(int userId) throws ErrorAddingUserException {
         try {
             String q = "insert into VotingUser values (?);";
             PreparedStatement st = conn.prepareStatement(q);
             st.setInt(1, userId);
             st.executeUpdate();
             conn.commit();
         } catch(SQLException e) {
            e.printStackTrace();
             try {
                 conn.rollback();
             } catch (Exception e2) {
                 //TODO What to do here?
                 System.err.println("Holy Fuck");
                 e2.printStackTrace();
             }
            throw new ErrorAddingUserException();
         }
     }
 
     public boolean globalVotingUserExists(int userId) {
         try {
             String q = "select count(*) from VotingUser where userId = ?";
             PreparedStatement st = conn.prepareStatement(q);
             st.setInt(1, userId);
             ResultSet rs = st.executeQuery();
             conn.commit();
             rs.next();
             return rs.getInt(1) == 1;
         } catch(SQLException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     public void addFormVotingUser(int userId, int formId) throws ErrorAddingUserException {
         try {
             String q = "insert into ValidVoter values(?, ?);";
             PreparedStatement st = conn.prepareStatement(q);
             st.setInt(1, userId);
             st.setInt(2, formId);
             st.executeUpdate();
             conn.commit();
         } catch (SQLException e) {
             e.printStackTrace();
             try {
                 conn.rollback();
             } catch(Exception e2) {
                 e2.printStackTrace();
             }
             throw new ErrorAddingUserException();
         }
     }
 
     public void removeFormVotingUser(int userId, int formId) throws InvalidVotingUserException {
         try {
             String q = "delete from ValidVoter where formId = ? and userId = ?;";
             PreparedStatement st = conn.prepareStatement(q);
             st.setInt(1, formId);
             st.setInt(2, userId);
             st.executeUpdate();
             conn.commit();
         } catch(SQLException e) {
             try {
                 conn.rollback();
             } catch(Exception e2) {
                 e2.printStackTrace();
             }
             throw new InvalidVotingUserException();
         }
     }
 
     public boolean isValidFormVoter(int userId, int formId) {
         try {
             String q = "select count(*) from ValidVoter where formId = ? and userId = ?";
             PreparedStatement st = conn.prepareStatement(q);
             st.setInt(1, formId);
             st.setInt(2, userId);
             ResultSet rs = st.executeQuery();
             conn.commit();
             rs.next();
             return rs.getInt(1) == 1;
         } catch(SQLException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     public void storeForm(int userId, Form form) throws SerialError, ErrorStoringFormException {
         try {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oout = new ObjectOutputStream(baos);
             oout.writeObject(form);
             oout.close();
             String q = "insert into Form (ownerId, formobject) values(?,?)";
             PreparedStatement st = conn.prepareStatement(q);
             st.setInt(1, userId);
             st.setBytes(2, baos.toByteArray());
             st.executeUpdate();
             conn.commit();
         } catch(IOException e) {
             e.printStackTrace();
             try {
                 conn.rollback();
             } catch(Exception e2) {
                 e2.printStackTrace();
             }
             throw new SerialError();
         } catch(SQLException e) {
             e.printStackTrace();
             try {
                 conn.rollback();
             } catch(Exception e2) {
                 e.printStackTrace();
             }
             throw new ErrorStoringFormException();
         }
     }
 
     public Integer[] getAllUserFormIds(int userId) {
         try {
            String q = "select * from form where ownerid = ?";
             PreparedStatement st = conn.prepareStatement(q);
             st.setInt(1, userId);
             ResultSet rs = st.executeQuery();
             conn.commit();
             ArrayList <Integer> forms = new ArrayList<Integer>();
             while(rs.next()) {
                 forms.add(rs.getInt(1));
             }
            //System.out.println(forms);
             return forms.toArray(new Integer[forms.size()]);
         } catch(SQLException e) {
             e.printStackTrace();
             try {
                 conn.rollback();
             } catch(Exception e2) {
                 e2.printStackTrace();
             }
             return new Integer[0];
         }
     }
 
     public Form getForm(int formId) throws FormNotFoundException, ErrorRecreatingFormException {
         try {
             String q = "select formobject from form where formid = ?";
             PreparedStatement st = conn.prepareStatement(q);
             st.setInt(1, formId);
             ResultSet rs = st.executeQuery();
             conn.commit();
             Form f;
             rs.next();
             byte[] buf = rs.getBytes(1);
             ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf));
             return (Form) in.readObject();
         }
         catch(SQLException e) {
             e.printStackTrace();
             throw new FormNotFoundException("Form does not exist");
         }
 
         catch(IOException e2) {
             e2.printStackTrace();
             throw new FormNotFoundException("IO Exception when retreiving form");
         }
         catch (ClassNotFoundException e3) {
             e3.printStackTrace();
             throw new ErrorRecreatingFormException("Error recreating form response");
         }
     }
 
     public boolean alreadyVoted(int userId, int formId) {
         try {
             String q1 = "select count(*) from formresponse where userid = ? and formid = ?;";
             PreparedStatement st1 = conn.prepareStatement(q1);
             st1.setInt(1, userId);
             st1.setInt(2, formId);
             ResultSet rs = st1.executeQuery();
             conn.commit();
             rs.next();
             return rs.getInt(1) > 0;
         } catch (SQLException e) {
             try {
                 e.printStackTrace();
                 conn.rollback();
             } catch (Exception e2) {
                 e2.printStackTrace();
             }
             return true;
         }
     }
 
     public void storeFormResponse(int userId, int formId, FormResponse resp) throws
             VoteNotStoredException, InvalidVotingUserException, FormNotFoundException, UserAlreadyVotedException {
         try {
             if(!this.isValidFormVoter(userId, formId)) {
                 throw new InvalidVotingUserException();
             }
             else if(!this.formExists(formId)) {
                 throw new FormNotFoundException("Cannot find form");
             }
             else if(this.alreadyVoted(userId, formId)) {
                 throw new UserAlreadyVotedException();
             }
             else {
                 String q2 = "insert into formresponse values(?, ?, ?);";
                 PreparedStatement st2 = conn.prepareStatement(q2);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oout = new ObjectOutputStream(baos);
                 oout.writeObject(resp);
                 oout.close();
                 st2.setInt(1, formId);
                 st2.setInt(2, userId);
                 st2.setBytes(3, baos.toByteArray());
                 st2.executeUpdate();
                 conn.commit();
             }
          }
         catch(IOException e) {
             e.printStackTrace();
             try {
                 conn.rollback();
             } catch(Exception e2) {
                 e2.printStackTrace();
                 System.out.println("Vote possibly contaminated!");
             }
             throw new VoteNotStoredException("Error storing vote");
         }
         catch(SQLException e2) {
             try {
                 conn.rollback();
             } catch(Exception e3) {
                 e2.printStackTrace();
                 System.out.println("Vote possibly contaminated!");
             }
             throw new VoteNotStoredException("Error storing vote");
         }
 
     }
 
     public boolean formExists(int formId) {
         try {
            String q = "select count(*) from form where formid = ?;";
             PreparedStatement st = conn.prepareStatement(q);
             st.setInt(1, formId);
             ResultSet rs = st.executeQuery();
             conn.commit();
             rs.next();
             return rs.getInt(1) > 0;
         } catch(Exception e) {
             try {
                 e.printStackTrace();
                 conn.rollback();
             } catch(Exception e2) {
                 e2.printStackTrace();
             }
             return false;
         }
 
     }
 
     public FormResponse[] getAllFormResponse(int formId) throws FormNotFoundException, ErrorRecreatingFormException {
         try {
             ArrayList<FormResponse> resp;
             if(!formExists(formId)) {
                 throw new FormNotFoundException("Form " + formId + " cannot be found");
             }
             String q = "select formresponse from FormResponse where formId = ?;";
             PreparedStatement st = conn.prepareStatement(q);
             st.setInt(1, formId);
             ResultSet rs = st.executeQuery();
             conn.commit();
             resp = new ArrayList<FormResponse>();
             while(rs.next()) {
                 byte[] buf = rs.getBytes(1);
                 ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf));
                 resp.add((FormResponse) in.readObject());
             }
             return resp.toArray(new FormResponse[resp.size()]);
         } catch (IOException e) {
             e.printStackTrace();
             try {
                 conn.rollback();
             }
             catch(Exception e4) {
                 e4.printStackTrace();
                 throw new FormNotFoundException("Error rolling back");
             }
             throw new ErrorRecreatingFormException("Error recreating form response");
         } catch(ClassNotFoundException e2) {
             e2.printStackTrace();
             try {
                 conn.rollback();
             }
             catch(Exception e4) {
                 e4.printStackTrace();
                 throw new FormNotFoundException("Error rolling back");
             }
             throw new ErrorRecreatingFormException("Error recreating form response");
         }
         catch(SQLException e3) {
             e3.printStackTrace();
             try {
                 conn.rollback();
             }
             catch(Exception e4) {
                 e4.printStackTrace();
                 throw new FormNotFoundException("Error rolling back");
             }
             throw new FormNotFoundException("Error getting responses");
         }
     }
 }
