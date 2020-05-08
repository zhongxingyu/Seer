 package de.hswt.hrm.contact.dao.jdbc;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.commons.dbutils.DbUtils;
 
 import static com.google.common.base.Preconditions.*;
 
 import de.hswt.hrm.common.database.DatabaseFactory;
 import de.hswt.hrm.common.database.NamedParameterStatement;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.contact.model.Contact;
 import de.hswt.hrm.contact.dao.core.IContactDao;
 
 public class ContactDao implements IContactDao {
 
     @Override
     public Collection<Contact> findAll() throws DatabaseException {
 
         final String query = "SELECT Contact_ID, Contact_Name, Contact_Zip_Code, "
                 + "Contact_City, Contact_Street, Contact_Street_Number, Contact_Shortcut, "
                 + "Contact_Phone, Contact_Fax, Contact_Mobile, Contact_Email FROM Contact ;";
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Contact> contacts = fromResultSet(result);
                 DbUtils.closeQuietly(result);
                 return contacts;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     @Override
     public Contact findById(int id) throws DatabaseException, ElementNotFoundException {
         checkArgument(id >= 0, "Id must not be negative.");
 
         final String query = "SELECT Contact_ID, Contact_Name, Contact_Zip_Code, "
                 + "Contact_City, Contact_Street, Contact_Street_Number, Contact_Shortcut, "
                 + "Contact_Phone, Contact_Fax, Contact_Mobile, Contact_Email FROM Contact "
                 + "WHERE Contact_ID = :id;";
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter("id", id);
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Contact> contacts = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 if (contacts.size() < 1) {
                     throw new ElementNotFoundException();
                 }
                 else if (contacts.size() > 1) {
                     throw new DatabaseException("ID '" + id + "' is not unique.");
                 }
 
                 return contacts.iterator().next();
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     /**
      * @see {@link IContactDao#insert(Contact)}
      */
     @Override
     public Contact insert(Contact contact) throws SaveException {
        final String query = "INSERT INTO Contact (Contact_Name "
                 + "Contact_Zip_Code, Contact_City, Contact_Street, Contact_Street_Number, "
                 + "Contact_Shortcut, Contact_Phone, Contact_Fax, Contact_Mobile, Contact_Email) "
                 + "VALUES (:name, :zipCode, :city, :street, :streetNumber, "
                 + ":shortcut, :phone, :fax, :mobile, :email);";
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter("name", contact.getName());
                 stmt.setParameter("zipCode", contact.getPostCode());
                 stmt.setParameter("city", contact.getCity());
                 stmt.setParameter("street", contact.getStreet());
                 stmt.setParameter("streetNumber", contact.getStreetNo());
                 stmt.setParameter("shortcut", contact.getShortcut().orNull());
                 stmt.setParameter("phone", contact.getPhone().orNull());
                 stmt.setParameter("fax", contact.getFax().orNull());
                 stmt.setParameter("mobile", contact.getMobile().orNull());
                 stmt.setParameter("email", contact.getEmail().orNull());
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
 
                 try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                     if (generatedKeys.next()) {
                         int id = generatedKeys.getInt(1);
 
                         // Create new contact with id
                         Contact inserted = new Contact(id, contact.getName(), contact.getStreet(),
                                 contact.getStreetNo(), contact.getPostCode(), contact.getCity());
 
                         inserted.setShortcut(contact.getShortcut().orNull());
                         inserted.setPhone(contact.getPhone().orNull());
                         inserted.setFax(contact.getFax().orNull());
                         inserted.setMobile(contact.getMobile().orNull());
                         inserted.setEmail(contact.getEmail().orNull());
 
                         return inserted;
                     }
                     else {
                         throw new SaveException("Could not retrieve generated ID.");
                     }
                 }
             }
 
         }
         catch (SQLException | DatabaseException e) {
             throw new SaveException(e);
         }
     }
 
     @Override
     public void update(Contact contact) throws ElementNotFoundException, SaveException {
         checkNotNull(contact, "Contact must not be null.");
 
         if (contact.getId() < 0) {
             throw new ElementNotFoundException("Element has no valid ID.");
         }
 
         final String query = "UPDATE Contact SET " + "Contact_Name = :name, "
                 + "Contact_Zip_Code = :zipCode, " + "Contact_City = :city, "
                 + "Contact_Street = :street, " + "Contact_Street_Number = :streetNumber, "
                 + "Contact_Shortcut = :shortcut, " + "Contact_Phone = :phone, "
                 + "Contact_Fax = :fax, " + "Contact_Mobile = :mobile, " + "Contact_Email = :email "
                 + "WHERE Contact_ID = :id;";
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter("id", contact.getId());
                 stmt.setParameter("name", contact.getName());
                 stmt.setParameter("zipCode", contact.getPostCode());
                 stmt.setParameter("city", contact.getCity());
                 stmt.setParameter("street", contact.getStreet());
                 stmt.setParameter("streetNumber", contact.getStreetNo());
                 stmt.setParameter("shortcut", contact.getShortcut().orNull());
                 stmt.setParameter("phone", contact.getPhone().orNull());
                 stmt.setParameter("fax", contact.getFax().orNull());
                 stmt.setParameter("mobile", contact.getMobile().orNull());
                 stmt.setParameter("email", contact.getEmail().orNull());
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
             }
         }
         catch (SQLException | DatabaseException e) {
             throw new SaveException(e);
         }
     }
 
     private Collection<Contact> fromResultSet(ResultSet rs) throws SQLException {
         checkNotNull(rs, "Result must not be null.");
         Collection<Contact> contactList = new ArrayList<>();
 
         while (rs.next()) {
             // Contact_Name, Contact_First_Name, Contact_Zip_Code, "
             // + "Contact_City, Contact_Street, Contact_Street_Number, Contact_Shortcut, "
             // + "Contact_Phone, Contact_Fax, Contact_Mobile, Contact_Email
             int id = rs.getInt("Contact_ID");
             String name = rs.getString("Contact_Name");
             String zipCode = rs.getString("Contact_Zip_Code");
             String city = rs.getString("Contact_City");
             String street = rs.getString("Contact_Street");
             String streetNo = rs.getString("Contact_Street_Number");
 
             Contact contact = new Contact(id, name, street, streetNo, zipCode, city);
             contact.setShortcut(rs.getString("Contact_Shortcut"));
             contact.setPhone(rs.getString("Contact_Phone"));
             contact.setFax(rs.getString("Contact_Fax"));
             contact.setMobile(rs.getString("Contact_Mobile"));
             contact.setEmail(rs.getString("Contact_Email"));
 
             contactList.add(contact);
         }
 
         return contactList;
     }
 }
