 package controllers;
 
 import java.util.*;
 import models.*;
 import play.data.binding.As;
 import play.data.validation.Required;
 import play.mvc.*;
 
 /**
  *
  * @author pierregaste
  */
 @With(Secure.class)
 public class Way extends Controller {
 
     /**
      * Action par défaut
      */
     public static void index() {
         redirect("/way/list");
     }
 
     /**
      * Listing des trajets
      */
     public static void list() {
         List<models.Way> ways = models.Way.find("driver = ? and deleted = 0 order by datehourstart", User.connected()).fetch();
        List<models.Way> ways_participate = new ArrayList<models.Way>();
        
         render(ways, ways_participate);
     }
 
     public static void add() {
 
         List<City> cities = City.find("order by name").fetch();
         List<models.Car> cars = models.Car.find("owner = ? order by name", User.connected()).fetch();
 
         render(cities, cars);
     }
 
     public static void addWay(@Required Long startCityId,
             @Required Long finishCityId,
             @Required Long carId,
             @Required Double cost,
             @Required Integer placeAvailable,
             @Required @As("dd/MM/yyyy") Date dateStart,
             @Required String hourStart,
             @Required Double minCost,
             @Required Double maxCost) {
 
         dateStart.setHours(Integer.parseInt(hourStart.split(":")[0]));
         dateStart.setMinutes(Integer.parseInt(hourStart.split(":")[1]));
         if (dateStart.getTime() < new Date().getTime()) {
             validation.addError("dateStart", "Date antérieur.");
         }
 
         validation.match(hourStart, "\\d\\d:\\d\\d").message("Heure invalide.");
         if (validation.hasErrors()) {
             params.flash(); // add http parameters to the flash scope
             validation.keep(); // keep the errors for the next request
             add();
         }
 
         City startCity = City.findById(startCityId);
         City finishCity = City.findById(finishCityId);
         models.User driver = User.connected();
         models.Car car = models.Car.findById(carId);
 
         models.Way way = new models.Way(startCity, finishCity, driver, dateStart, car, placeAvailable, cost, minCost, maxCost);
         try {
             way.calculateWay();
         } catch (Exception ex) {
             params.put("fail", "error during the calcul");
             params.flash(); // add http parameters to the flash scope
             validation.keep(); // keep the errors for the next request
             add();
         }
         way.save();
         redirect("/way/list");
     }
 
     public static void calculCost(Long startCityId, Long finishCityId, Long carId) throws Exception {
         models.City startCity = models.City.findById(startCityId);
         models.City finishCity = models.City.findById(finishCityId);
         models.Car car = models.Car.findById(carId);
 
         List<Object> jsonReturn = new ArrayList<Object>();
         jsonReturn.add(new models.Way(startCity, finishCity, car).cost());
         jsonReturn.add(car.nbPlace);
 
         renderJSON(jsonReturn);
     }
 
     public static void edit(Long id) {
         models.Way way = models.Way.findById(id);
         List<City> cities = City.find("order by name").fetch();
         List<models.Car> cars = models.Car.find("owner = ? order by name", User.connected()).fetch();
 
         render(way, cities, cars);
     }
 
     public static void editWay(Long id,
             @Required Long startCityId,
             @Required Long finishCityId,
             @Required Double cost,
             @Required Long carId,
             @Required Integer placeAvailable,
             @Required @As("dd/MM/yyyy") Date dateStart,
             @Required String hourStart,
             @Required Double minCost,
             @Required Double maxCost) {
 
         dateStart.setHours(Integer.parseInt(hourStart.split(":")[0]));
         dateStart.setMinutes(Integer.parseInt(hourStart.split(":")[1]));
         if (dateStart.getTime() < new Date().getTime()) {
             validation.addError("dateStart", "Date antérieur.");
         }
 
         validation.match(hourStart, "\\d\\d:\\d\\d").message("Heure invalide.");
         if (validation.hasErrors()) {
             params.flash(); // add http parameters to the flash scope
             validation.keep(); // keep the errors for the next request
             edit(id);
         }
 
         City startCity = City.findById(startCityId);
         City finishCity = City.findById(finishCityId);
         models.Car car = models.Car.findById(carId);
 
         models.Way way = models.Way.findById(id);
         way.startCity = startCity;
         way.finishCity = finishCity;
         way.dateHourStart = dateStart;
         way.car = car;
         way.cost = cost;
         way.minCost = minCost;
         way.maxCost = maxCost;
 
         try {
             way.calculateWay();
         } catch (Exception ex) {
             params.put("fail", "error during the calcul");
             params.flash(); // add http parameters to the flash scope
             validation.keep(); // keep the errors for the next request
             add();
         }
         way.save();
         redirect("/way/list");
     }
 
     public static void details(Long id) {
         models.Way way = models.Way.findById(id);
         render(way);
     }
 
     public static void cancel(Long id) {
         models.Way way = models.Way.findById(id);
 
         if (way != null && way.driver == User.connected()) {
             List<models.WayParticipation> participants = models.WayParticipation.find("byWay", way).fetch();
 
             if (participants.isEmpty()) {
                 way.delete();
             } else {
                 way.deleted = 1;
                 for (models.WayParticipation participant : participants) {
                     participant.status = 3;
                     participant.save();
                 }
                 way.save();
             }
         }
 
         redirect("/way/list");
     }
 
     public static void search(Long startCityId, Long finishCityId, @As("dd/MM/yyyy") Date fromDate) {
         models.City startCity = models.City.findById(startCityId);
         models.City finishCity = models.City.findById(finishCityId);
         List<models.Way> ways = new ArrayList<models.Way>();
 
         if (startCity == null || finishCity == null) {
             for (models.User friend : User.connected().friends()) {
                 for (models.Way way : friend.ways_driver()) {
                    ways.add(way);
                }
            }
            ways = removeParticipationInArray(ways);
            render(ways);
        }

        for (models.User friend : User.connected().friends()) {
            for (models.Way way : friend.ways_driver()) {
                if (way.startCity == startCity && way.finishCity == finishCity && way.dateHourStart.getTime() >= fromDate.getTime()) {
                    ways.add(way);
                 }
             }
         }
 
         ways = removeParticipationInArray(ways);
         render(ways);
     }
 
     private static List<models.Way> removeParticipationInArray(List<models.Way> ways) {
         List<models.WayParticipation> wayParticipations;
         ArrayList<models.Way> participations = new ArrayList<models.Way>();
 
         wayParticipations = models.WayParticipation.find("participant = ? and status != 1", User.connected()).fetch();
 
         for (models.WayParticipation participation : wayParticipations) {
             participations.add(participation.way);
         }
         ways.removeAll(participations);
 
         for (models.Way way : ways) {
             if (way.placeRemain() == 0) {
                 ways.remove(way);
             }
         }
 
         return ways;
     }
 }
