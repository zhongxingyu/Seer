 package com.esiea.controller;
 
 import com.esiea.core.Address;
 import com.esiea.core.Appz;
 import com.esiea.core.Contact;
 import com.esiea.core.User;
 import java.util.ArrayList;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class ContactController {
     // note pour faciliter la lecture les request mapping sont dans l'ordre alphabetique
 
     @RequestMapping(value = "/add_addr")
     public CustomModelAndView add_addr_page(HttpServletRequest hsr, HttpServletResponse hsr1) {
         return new CustomModelAndView(hsr, hsr1, "/appz/add_addr");
     }
 
     @RequestMapping(value = "/add_addr_validator")
     public CustomModelAndView add_addr_validator(HttpServletRequest hsr, HttpServletResponse hsr1) {
         try {
             User user = ServerUtils.getUser(hsr, hsr1);
             Contact tmpContact = (Contact) hsr.getSession().getAttribute("contactOject");
             String nickaddress = hsr.getParameter("nickaddress").toString();
             String nb = hsr.getParameter("addr_nb").toString();
             String rue = hsr.getParameter("addr_rue").toString();
             String ville = hsr.getParameter("addr_ville").toString();
             String cp = hsr.getParameter("addr_cp").toString();
             String pays = hsr.getParameter("addr_pays").toString();
 
             Address tmpAddr = new Address(nickaddress, nb, rue, ville, cp, pays);
             user.getUserData().InsertAddressAssociatedToContact(tmpContact, tmpAddr);
             return new CustomModelAndView(hsr, hsr1, "redirect:/list_show.html");
 
         } catch (Exception e) {
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
         }
     }
 
     @RequestMapping(value = "/add_contact")
     public CustomModelAndView add_contact_page(HttpServletRequest hsr, HttpServletResponse hsr1) {
         return new CustomModelAndView(hsr, hsr1, "/appz/add_contact");
     }
 
     @RequestMapping(value = "/add_contact_validator", method = RequestMethod.POST)
     public CustomModelAndView add_contact_validator(HttpServletRequest hsr, HttpServletResponse hsr1) {
         try {
             ServerUtils.getUser(hsr, hsr1);
             String prenom = hsr.getParameter("prenom").toString();
             String nom = hsr.getParameter("nom").toString();
             String phone = hsr.getParameter("phone").toString();
             String mail = hsr.getParameter("mail").toString();
             String birthday = hsr.getParameter("birthday").toString();
            
            boolean actif = Boolean.valueOf(hsr.getParameter("actif"));
            //Contact tmpCont = new Contact(nom, prenom, mail, phone, birthday);
            Contact tmpCont = new Contact(nom, prenom, mail, phone, birthday,actif);
             hsr.getSession().setAttribute("contactOject", tmpCont);
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/appz/add_contact_display");
             customModelAndView.addObject("tmpCont", tmpCont);
             return customModelAndView;
         } catch (Exception e) {
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
 
         }
     }
 
     @RequestMapping(value = "/delete", method = RequestMethod.GET)
     public CustomModelAndView delete(HttpServletRequest hsr, HttpServletResponse hsr1) {
         try {
             int index = Integer.valueOf(hsr.getParameter("deleteID"));
             User user = ServerUtils.getUser(hsr, hsr1);
             Contact contactToRemove = user.getUserData().getTableContact().get(index);
             user.getUserData().removeContact(contactToRemove);
             ArrayList<Contact> arrContact = user.getUserData().getTableContact();
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/appz/list_show");
             customModelAndView.addObject("arrContact", arrContact);
             return customModelAndView;
         } catch (Exception e) {
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
         }
     }
 
     @RequestMapping(value = "/list_add", method = RequestMethod.GET)
     public CustomModelAndView list_addr_page(HttpServletRequest hsr, HttpServletResponse hsr1) {
         try {
             User usrLogin = ServerUtils.getUser(hsr, hsr1);
             ArrayList<Contact> arrContact = usrLogin.getUserData().getTableContact();
             // on test si pour le contact selectionner il y a des addresses a afficher (et aussi si le contact existe)
             // sinon retour acceuil
             if (!arrContact.isEmpty()) {
                 Contact ctct = arrContact.get(Integer.valueOf(hsr.getParameter("contactID")));
                 ArrayList<Address> addrs = usrLogin.getUserData().getAddressAssociatedToContact(ctct);
                 if (!addrs.isEmpty()) {
                     CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/appz/list_add");
                     customModelAndView.addObject("addrs", addrs);
                     return customModelAndView;
                 } else {
                     return new CustomModelAndView(hsr, hsr1, "/index");
                 }
 
             } else {
                 return new CustomModelAndView(hsr, hsr1, "/index");
             }
 
         } catch (Exception e) {
             hsr.getSession().invalidate();
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
         }
     }
 
     @RequestMapping(value = "/list_show", method = RequestMethod.GET)
     public CustomModelAndView list_show_page(HttpServletRequest hsr, HttpServletResponse hsr1) {
         try {
             User user = ServerUtils.getUser(hsr, hsr1);
             ArrayList<Contact> arrContact = user.getUserData().getTableContact();
             CustomModelAndView modelAndView = new CustomModelAndView(hsr, hsr1, "/appz/list_show");
             modelAndView.addObject("arrContact", arrContact);
             return modelAndView;
         } catch (Exception e) {
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
         }
     }
 
     @RequestMapping(value = "/modify_contact", method = RequestMethod.GET)
     public CustomModelAndView modify_contact_page(HttpServletRequest hsr, HttpServletResponse hsr1) {
         try {
             //indice  modID arraylist pour choix contact  
             Integer indice = Integer.valueOf(hsr.getParameter("modID"));
             User user = ServerUtils.getUser(hsr, hsr1);
             Contact contact = user.getUserData().getTableContact().get(indice);
             hsr.getSession().setAttribute("MODcontactID", indice);
             hsr.getSession().setAttribute("modcontactID", indice);
             CustomModelAndView modelAndView = new CustomModelAndView(hsr, hsr1, "/appz/modify_contact");
             modelAndView.addObject("contact", contact);
             modelAndView.addObject("MODcontactID", indice);
             return modelAndView;
         } catch (Exception e) {
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
         }
     }
 
     @RequestMapping(value = "/modify_contact_v")
     public CustomModelAndView modify_contact_validator(HttpServletRequest hsr, HttpServletResponse hsr1) {
         try {
             User user = ServerUtils.getUser(hsr, hsr1);
 
             String prenom = hsr.getParameter("prenom").toString();
             String nom = hsr.getParameter("nom").toString();
             String phone = hsr.getParameter("phone").toString();
             String mail = hsr.getParameter("mail").toString();
             String birthday = hsr.getParameter("birthday").toString();
            boolean actif = Boolean.valueOf(hsr.getParameter("actif"));
            //Contact tmpCont = new Contact(nom, prenom, mail, phone, birthday);
            Contact tmpCont = new Contact(nom, prenom, mail, phone, birthday,actif);
             int modContactID = Integer.valueOf(hsr.getSession().getAttribute("MODcontactID").toString());
             Contact old = user.getUserData().getTableContact().get(modContactID);
             //TODO test unique contact
             Contact modifyCtct = new Contact(nom, prenom, mail, phone, birthday);
             user.getUserData().modifyContact(old, modifyCtct);
 
             ArrayList<Contact> arrContact = user.getUserData().getTableContact();
             CustomModelAndView modelAndView = new CustomModelAndView(hsr, hsr1, "/appz/list_show");
             modelAndView.addObject("arrContact", arrContact);
             return modelAndView;
         } catch (Exception e) {
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
         }
     }
 
     @RequestMapping(value = "/modify_addrs")
     public CustomModelAndView modify_adrrs_page(HttpServletRequest hsr, HttpServletResponse hsr1) {
         try {
             User user = ServerUtils.getUser(hsr, hsr1);
             int modContactID = Integer.valueOf(hsr.getSession().getAttribute("MODcontactID").toString());
             ArrayList<Contact> arrContact = user.getUserData().getTableContact();
             // on teste si l'adresse existe pour le contact selectionner 
             // sinon retour page acceuil
             if (!arrContact.isEmpty()) {
                 Contact ctct = arrContact.get(modContactID);
                 ArrayList<Address> addrs = user.getUserData().getAddressAssociatedToContact(ctct);
                 if (!addrs.isEmpty()) {
                     CustomModelAndView modelAndView = new CustomModelAndView(hsr, hsr1, "/appz/modify_addrs");
                     modelAndView.addObject("addrs", addrs);
                     return modelAndView;
                 } else {
                     return new CustomModelAndView(hsr, hsr1, "/index");
                 }
             } else {
                 return new CustomModelAndView(hsr, hsr1, "/index");
             }
         } catch (Exception e) {
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
         }
     }
 
     @RequestMapping(value = "/modify_addr", method = RequestMethod.GET)
     public CustomModelAndView modify_adrr_page(HttpServletRequest hsr, HttpServletResponse hsr1) {
         try {
             int modAddrID = Integer.valueOf(hsr.getParameter("modaddrID"));
             hsr.getSession().setAttribute("modaddrID", modAddrID);
             int modContactID = Integer.valueOf(hsr.getSession().getAttribute("MODcontactID").toString());
             User user = ServerUtils.getUser(hsr, hsr1);
 
             ArrayList<Contact> arrContact = user.getUserData().getTableContact();
             // on teste si l'adresse existe pour le contact selectionner 
             // sinon retour page acceuil
             if (!arrContact.isEmpty()) {
                 Contact ctct = arrContact.get(modContactID);
                 Address addr = user.getUserData().getAddressAssociatedToContact(ctct).get(modAddrID);
                 if (addr != null) {
                     CustomModelAndView modelAndView = new CustomModelAndView(hsr, hsr1, "/appz/modify_addr");
                     modelAndView.addObject("addr", addr);
                     return modelAndView;
                 } else {
                     return new CustomModelAndView(hsr, hsr1, "/index");
                 }
             } else {
                 return new CustomModelAndView(hsr, hsr1, "/index");
             }
         } catch (Exception e) {
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
         }
     }
 
     @RequestMapping(value = "/modify_addr_v", method = RequestMethod.POST)
     public CustomModelAndView modify_adrr_validator(HttpServletRequest hsr, HttpServletResponse hsr1) {
         try {
             int modAddrID = Integer.valueOf(hsr.getSession().getAttribute("modaddrID").toString());
             int modContactID = Integer.valueOf(hsr.getSession().getAttribute("MODcontactID").toString());
             User user = ServerUtils.getUser(hsr, hsr1);
 
             ArrayList<Contact> arrContact = user.getUserData().getTableContact();
             // on teste si l'adresse existe pour le contact selectionner 
             // sinon retour page acceuil
             if (!arrContact.isEmpty()) {
                 Contact ctct = arrContact.get(modContactID);
                 Address addr = user.getUserData().getAddressAssociatedToContact(ctct).get(modAddrID);
                 if (addr != null) {
 
                     String nb = hsr.getParameter("addr_nb").toString();
                     String rue = hsr.getParameter("addr_rue").toString();
                     String ville = hsr.getParameter("addr_ville").toString();
                     String cp = hsr.getParameter("addr_cp").toString();
                     String pays = hsr.getParameter("addr_pays").toString();
                     //  String nick = hsr.getParameter("nickaddress").toString();
 
                     Address newAddr = new Address(nb, rue, ville, cp, pays);
                     Address oldAddr = user.getUserData().getTableAddress().get(modAddrID);
 
                     oldAddr.update(newAddr);
 
                     CustomModelAndView modelAndView = new CustomModelAndView(hsr, hsr1, "/appz/list_show");
                     modelAndView.addObject("arrContact", arrContact);
                     return modelAndView;
                 } else {
                     return new CustomModelAndView(hsr, hsr1, "/index");
                 }
             } else {
                 return new CustomModelAndView(hsr, hsr1, "/index");
             }
         } catch (Exception e) {
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
         }
     }
 
     @RequestMapping(value = "/search", method = RequestMethod.GET)
     public CustomModelAndView search_page(HttpServletRequest hsr, HttpServletResponse hsr1) {
         try {
             User user = ServerUtils.getUser(hsr, hsr1);
             CustomModelAndView modelAndView = new CustomModelAndView(hsr, hsr1, "/appz/search");
             return modelAndView;
         } catch (Exception e) {
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
         }
     }
 
     @RequestMapping(value = "/search_find", method = RequestMethod.POST)
     public CustomModelAndView search_results(HttpServletRequest hsr, HttpServletResponse hsr1) {
         try {
             User user = ServerUtils.getUser(hsr, hsr1);
             String stringPattern = hsr.getParameter("stringPattern").toString();
             ArrayList<Contact> arrContact = user.getUserData().searchContact(user.getUsername(), stringPattern, Appz.getInstance());
             ArrayList<Address> addrs = user.getUserData().searchAddr(user.getUsername(), stringPattern, Appz.getInstance());
             CustomModelAndView modelAndView = new CustomModelAndView(hsr, hsr1, "/appz/search_found");
             modelAndView.addObject("arrContact", arrContact);
             modelAndView.addObject("addrs", addrs);
             return modelAndView;
         } catch (Exception e) {
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
         }
     }
 
     @RequestMapping(value = "/add_addr_from_modify", method = RequestMethod.POST)
     public CustomModelAndView add_addr_from_modify_page(HttpServletRequest hsr, HttpServletResponse hsr1) {
 
         try {
             User user = ServerUtils.getUser(hsr, hsr1);
           //  int modcontactID = Integer.valueOf(hsr.getParameter("modcontactID"));
            int modcontactID =  Integer.valueOf(hsr.getSession().getAttribute("modcontactID").toString());
             //hsr.getSession().setAttribute("modcontactID", modcontactID);
             CustomModelAndView modelAndView = new CustomModelAndView(hsr, hsr1, "/appz/add_addr_from_modify");
             return modelAndView;
         } catch (Exception e) {
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
         }
     }
 
     @RequestMapping(value = "/add_addr_from_modify_v", method = RequestMethod.POST)
     public CustomModelAndView add_addr_from_modify_validator(HttpServletRequest hsr, HttpServletResponse hsr1) {
         try {
             User user = ServerUtils.getUser(hsr, hsr1);
             
             int modContactID = Integer.valueOf(hsr.getSession().getAttribute("MODcontactID").toString());
             Contact tmpContact = user.getUserData().getTableContact().get(modContactID);
             String nickaddress = hsr.getParameter("nickaddress").toString();
             String nb = hsr.getParameter("addr_nb").toString();
             String rue = hsr.getParameter("addr_rue").toString();
             String ville = hsr.getParameter("addr_ville").toString();
             String cp = hsr.getParameter("addr_cp").toString();
             String pays = hsr.getParameter("addr_pays").toString();
 
             Address tmpAddr = new Address(nickaddress, nb, rue, ville, cp, pays);
             user.getUserData().InsertAddressAssociatedToContact(tmpContact, tmpAddr);
             
             return new CustomModelAndView(hsr, hsr1, "redirect:/list_show.html");
         } catch (Exception e) {
             CustomModelAndView customModelAndView = new CustomModelAndView(hsr, hsr1, "/error");
             customModelAndView.addObject("str", e.getMessage());
             return customModelAndView;
         }
     }
 }
