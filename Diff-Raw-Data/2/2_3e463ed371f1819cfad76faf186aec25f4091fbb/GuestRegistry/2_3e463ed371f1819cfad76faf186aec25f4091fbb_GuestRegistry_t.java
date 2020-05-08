 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package prosjekt.guests;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author Kristoffer Berdal <web@flexd.net>
  * @since 2012-04-16
  */
 public class GuestRegistry {
   // "Indeksert" etter from, to og room.
   private ArrayList<AbstractGuest> list = new ArrayList();
   
   /*
    * @param AbstractGuest guest
    * @returns true/false
    */
   //TODO: VALIDERING
   public boolean add(AbstractGuest guest) {
     if (exists(guest)) {
       return false;
     }
     list.add(guest);
     return true;
   }
   /*
    * @return List of guests (all guests, for all history)
    */
   public ArrayList<AbstractGuest> getList() {
     return list;
   }
   /*
    * @param guest guest to remove!
    * @return true/false om gjesten ble fjernet/fantes!
    */
   public boolean remove(AbstractGuest guest) {
     for (AbstractGuest g : list) {
       if (g.equals(guest)) {
         list.remove(g);
         return true;
       }
     }
     return false;
   }
   /*
    * @param firstName Fornavn til gjest du skal finne
    * @param lastName  Etternavn til gjest du skal finne.
    * @param phoneNumber Telefonnummer til gjest du skal finne.
    */
   //TODO: Normaliser telefonnummer med regulært utrykk: Hvis noen skriver f.eks 93 82 81 06 må vi kunne matche det mot 93828106 osv.
   public AbstractGuest getGuest(String firstName, String lastName, String phoneNumber) {
     for (AbstractGuest g : list) {
       if (g.getFirstName().equals(firstName) && g.getLastName().equals(lastName) && g.getPhoneNumber().equals(phoneNumber)) {
         return g;
       }
     }
     return null;
   }
   /*
    * @param guest Guest to find
    * @returns AbstractGuest guest
    */
   public AbstractGuest getGuest(AbstractGuest guest) {
     for (AbstractGuest g : list) {
       if (g.equals(guest)) {
         return g;
       }
     }
     return null;
   }
   /*
    * @param AbstractGuest guest, guest to find
    * @return true/false
    */
   public boolean exists(AbstractGuest guest) {
     for (AbstractGuest g : list) {
      if (g.getID() == guest.getID()) {
         return true;
       }
      }
     return false;
   }
 }
