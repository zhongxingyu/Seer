 package controllers;
 
 import models.*;
 import play.data.DynamicForm;
 import play.mvc.Result;
 import views.html.reserve;
 import views.html.reserve_apartment_proposal;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import static play.data.Form.form;
 import static play.mvc.Results.ok;
 
 public class Reserve {
 
     public static Result reserve() {
         return ok(views.html.reserve.render(Hotel.all()));
     }
 
     public static Result reserveHotel(Long id) {
         // TODO: load required apartment types
         List<ApartmentType> apartmentTypes = ApartmentType.all();
         return ok(views.html.reserve_hotel.render(Hotel.get(id), apartmentTypes));
     }
 
     public static Result reserveApartmentType(Long hotelId, Long apartmentTypeId) {
         // TODO: load required apartment proposals
         List<ApartmentProposal> apartmentProposals = ApartmentProposal.all();
         return ok(views.html.reserve_apartment_type.render(ApartmentType.get(apartmentTypeId), apartmentProposals));
     }
 
     public static Result reserveProposal(long proposalId) {
         ApartmentProposal apartmentProposal = ApartmentProposal.get(proposalId);
 
         // TODO: Dirty hack because of ebean doesn't load dependencies by default
         Apartment apartment = Apartment.get(apartmentProposal.apartment.id);
         apartmentProposal.setApartment(apartment);
 
        ApartmentType apartmentType = ApartmentType.get(apartment.id);
         apartment.setApartmentType(apartmentType);
 
         return ok(reserve_apartment_proposal.render(apartmentProposal));
     }
 
     public static Result reserveProposalByClient(long proposalId) {
         DynamicForm dynamicForm = form().bindFromRequest();
 
         String name = dynamicForm.get("name");
         String surname = dynamicForm.get("surname");
         String email = dynamicForm.get("email");
 
         Client client = new Client(name, surname, email);
         client = Client.saveIfNotPresent(client);
 
         ApartmentProposal apartmentProposal = ApartmentProposal.get(proposalId);
 
         String bookedFrom = dynamicForm.get("bookedFrom");
         String bookedTo = dynamicForm.get("bookedTo");
 
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
 
         try {
             ApartmentHistory apartmentHistory = new ApartmentHistory(dateFormat.parse(bookedFrom),
                     dateFormat.parse(bookedTo), apartmentProposal, client);
             ApartmentHistory.save(apartmentHistory);
         } catch (ParseException e) {
 
         }
 
         return ok(reserve.render(Hotel.all()));
     }
 }
