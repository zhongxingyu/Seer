 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package languages;
 
 import java.util.ListResourceBundle;
 
 /**
  *
  * @author Flo
  */
 public class ResourceBundle extends ListResourceBundle {
 
     @Override
     protected Object[][] getContents() {
         return contents;
     }
 
     private Object[][] contents = {
         
         // Universally used strings
             { "add", "Add"},
             { "address", "Address" },            
             { "adjust", "Adjust" },
             { "allFields", "All fields" },
             { "cancel", "Cancel" },
             { "city", "City" },
             { "country", "Country" },
             { "create", "Create" },
             { "customerId", "Customer ID" },
             { "description", "Description" },
             { "email", "Email" },
             { "employee", "Employee" },
             { "exit", "Exit" },
             { "fillInAllFields", "Please fill in all fields and try again."},
             { "firstName", "First name" },
             { "found", "Found" },
             { "handled", "Handled" },
             { "lastChangedBy", "Last changed by " },
             { "lastName", "Last name" },
             { "linkCustomer", "Link customers" },
             { "location", "Location" },
             { "login" , "Login" },
             { "lost", "Lost" },
             { "luggage", "Luggage" },
             { "manager", "Manager" },
             { "name", "Name" },
             { "ok", "Ok" },
             { "on", "on" },
             { "options", "Options" },
             { "overviews", "Overviews" },
             { "password", "Password" },
             { "phoneHome", "Phone - home" },
             { "phoneNumber", "Phone number" },
             { "phoneMobile", "Phone - mobile" },
             { "postalCode", "Postal code" },
             { "reset", "Reset" },
             { "resetOverview", "Refresh Overview" },
             { "resort", "Resort" },
             { "resortTab", "Resort overview and registration" },
             { "search", "Search" },
             { "selectItemInTable", "Please select an item from the table and try again." },
             { "status", "Status" },
             { "unlockFields", "Unlock fields" },
             { "userId", "User Id" },
             { "username", "Username" },
             { "update", "Update" },
             
         // StringArrays
             { "customerSearchFields", new String[] { "All fields", "Customer ID", "Firstname",
                 "Lastname", "Address", "Postal code", "City", "Country",
                 "Email", "Phone - Home", "Phone - Mobile"} },
             
             { "customerSearchFieldsMin", new String[] { "All fields", "Customer ID", "Firstname",
                 "Lastname", "Address", "Postal code", "City", "Country",
                 "Email"} },
             
             { "customerTableFields", new String[] { "Customer ID", "Firstname",
                 "Lastname", "Address", "Postal code", "City", "Country",
                 "Email", "Phone - Home", "Phone - Mobile"} },
             
             { "customerTableFieldsMin", new String[] { "Customer ID", "Firstname",
                 "Lastname", "Address", "Postal code", "City", "Country",
                 "Email"} },
             
             { "luggageSearchFields", new String [] { "All fields", "Luggage ID",
                 "Customer ID", "Description", "Location", "Date lost" } },
             
             { "luggageTableFields", new String[] { "Luggage ID", "Customer ID",
                 "Description", "Location", "Date lost", "Status" } },
             
             { "months", new String[] { "January", "February", "March", "April",
                 "May", "June", "July", "August", "September", "October",
                 "November", "December" } },
             
             { "statuses", new String[] { "Lost", "Found", "Handled" } },
             
             { "userSearchFields", new String[] { "All fields", "User ID", "Firstname",
                 "Lastname", "Username" } },
             
             { "userTableFields", new String[] { "Username", "Firstname",
                 "Lastname", "User Group", "Account State" } },
             
             { "userTypes", new String[] { "Employee", "Manager", "Administrator" } },
             
             { "countriesComboBox", new String[] { "Netherlands", "Turkey", "Australia" } },
             
             { "resortTableTitles", new String[] { "Resort ID", "Resort name", 
                 "Address", "Country", "City", "Phone number", "Email", "Postal Code" } },
             
             { "resortTableAllFieldsLink", new String [] { "All fields", "Name",
                 "Adress", "Country", "City", "Phone number", "Email", "Postal Code" } },
             
             { "resortTableAllFields", new String[] { "Resort ID", "Resort name", 
                 "Address", "Country", "City", "Phone number", "Email", "Postal Code" } },
             
             
             
         // Toolbar
             { "changePassword", "Change Password" },
             { "logout", "Logout" },
             
         // Login.java
             { "accountHasBeenLocked", "This account has been locked. \n"
                 + "Please contact your administrator." },
             { "loginIncorrect", "Username/Password is incorrect." },
             
             
             //ResetPassword.java
             {"changePasswordForUser", "Change password" },
             {"apply", "Apply"},
             
             
         // ChangeMyPassword.java           
             { "changeMyPassword", "Change Password" },
             { "currentPasswordIncorrect", "Current password is incorrect." },
             { "newPasswordNoMatch", "New password fields do not match." },
             { "typeCurrentPassword", "Enter current password" },
             { "typeNewPassword", "Enter new password" },
             { "typeRepeatPassword", "Repeat new password" },
             
         // Administrator.java
             { "accountState", "Account state" },
             { "accountIsUnlocked",
                 "The account has successfully been unlocked." },
             { "accountIsLocked",
                 "The account has successfully been locked." },
             { "active", "Active" },
             { "administrator", "Administrator" },
             { "createUserPrompt", 
                 "Are you sure you want to create this user account?" },
             { "deleteUserPrompt", 
                 "Are you sure you want to delete this user account?" },
             { "deleteUser", "Delete user" },
             { "editInfo", "Edit user information" },
             { "lockAccount", "Lock account" },
             { "locked", "Locked" },
             { "resetPassword", "Reset password" },
             { "unlockAccount", "Unlock account" },
             { "userGroup", "User group" },
             { "usernameInUse", "Username is already in use." },
             { "userOptions", "User options" },
             { "userOverview", "User overview" },
             { "createUser", "Create user" },
             
         // Manager.java
             { "allData", "All Data" },
             { "allStatusLuggage", "Missing, found and handled luggage per month" },
             { "beginDate", "The first date can't be more than end date." },
             { "foundLuggage", "Found Luggage" },
             { "from", "From" },
             { "handledLuggage", "Handled Luggage" },
             { "missingLuggage", "Missing Luggage" },
             { "month", "Month" },
             { "options", "Options" },
             { "otherOptions", "Other Options" },
             { "seeAllData1", "Shows all data, even if the status of the" },
             { "seeAllData2", "luggage is no longer applicable" },
             { "to", "To" },
             { "xAxis", "Month/Year" },
             { "yAxis", "Number" },
             { "year", "Year" },
             // Month abbreviations
             { "Jan", "Jan" },
             { "Feb", "Feb" },
             { "Mar", "Mar" },
             { "Apr", "Apr" },
             { "May", "May" },
             { "Jun", "Jun" },
             { "Jul", "Jul" },
             { "Aug", "Aug" },
             { "Sep", "Sep" },
             { "Oct", "Oct" },
             { "Nov", "Nov" },
             { "Dec", "Dec" },
             
           
             
         // Employee.java
             { "hideHandled", "Hide handled luggage" },
             { "unassigned", "Unassigned"},
             { "found", "Found"},
             { "lost", "Lost"},
             { "handled", "Handled"},
             { "registerCustomer", "Register Customer"},
             { "customer", "Klant"},
             { "extensiveCustomerData", "Extended customer data" },
             { "deleteCustomer", "Delete customer" },
             { "summaryCustomerRegister", "Customer summary and registration" },
             { "registerLuggage", "Register luggage"},
             { "modifyLuggage", "Modify luggage"},
             { "proofPrint", "Print receipt"},
             { "deleteLuggage", "Delete luggage"},
             { "summaryLuggageRegister", "Luggage summary and registration"},
             { "summary", "Summary"},
             { "connect", "Link"},
             { "newResort", "New resort"},
             { "updateResort", "Update resort"},
             { "deleteResort", "Delete resort"},
             { "createResort", "Create resort"},
             { "summaryResort", "Resort summary"},
            
             
             
         // ExtendedCustomer.java
             { "changeCustomerInfo", "Change customer information" },
             { "clearList", "Remove all items" },
             { "deleteFromList", "Remove selected item" },
             { "itemsToPrint", "List of items that will be printed" },
             { "printTicket", "Print luggage ticket" },
             { "resortInfo", "Resort information" },
             { "addToPrintList", "Add to printlist"},
             { "hideHandled", "Hide handled luggage"},
             { "hideMissing", "Hide missing luggage"},
             { "hideFound", "Hide found luggage"},
     };
     
     
     
     
 }
