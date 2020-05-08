 package controllers;
 
 import java.util.*;
 import models.*;
 import play.data.binding.As;
 import play.data.validation.Required;
 import play.mvc.*;
 
 @With(Secure.class)
 public class Way extends Controller {
 
     /**
      * [GET] Route par défaut
      * 
      */
     public static void index() {
         redirect("/way/list");
     }
 
     /**
      * [GET] Liste des trajets concernant l'utilisateur connecté
      * 
      */
     public static void list() {
         List<models.Way> ways = models.Way.find("driver = ? and deleted = 0 order by datehourstart", User.connected()).fetch();
        List<models.WayParticipation> ways_participation = models.WayParticipation.find("participant = ? and (status = 2 or status = 4)", User.connected()).fetch();
 
         render(ways, ways_participation);
     }
 
     /**
      * [GET] Formulaire d'ajout de trajet
      * 
      */
     public static void add() {
         List<City> cities = City.find("order by name").fetch();
         List<models.Car> cars = models.Car.find("owner = ? and deleted = 0 order by name", User.connected()).fetch();
 
         render(cities, cars);
     }
 
     /**
      * [GET] Formulaire d'édition de trajet
      * 
      * @param id 
      */
     public static void edit(Long id) {
         models.Way way = models.Way.findById(id);
         List<City> cities = City.find("order by name").fetch();
         List<models.Car> cars = models.Car.find("owner = ? and deleted = 0 order by name", User.connected()).fetch();
 
         render(way, cities, cars);
     }
 
     /**
      * [GET] Page de détails d'un trajet
      * 
      * @param id 
      */
     public static void details(Long id) {
         models.Way way = models.Way.findById(id);
         render(way);
     }
 
     /**
      * [GET] Requete d'annulation d'un trajet
      * 
      * @param id 
      */
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
 
     /**
      * [GET] Page de recherche de trajet
      * 
      */
     public static void search() {
         List<models.Way> ways = new ArrayList<models.Way>();
         List<City> cities = City.find("order by name").fetch();
 
         for (models.User friend : User.connected().friends()) {
             for (models.Way way : friend.ways_driver()) {
                 ways.add(way);
             }
         }
         ways = _removeParticipationInArray(ways);
         render(ways, cities);
     }
 
     /**
      * [POST] Soumission du formulaire d'ajout du trajet
      * 
      * @param startCityId
      * @param finishCityId
      * @param carId
      * @param cost
      * @param placeAvailable
      * @param dateStart
      * @param hourStart
      * @param minCost
      * @param maxCost
      */
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
             params.flash();
             validation.keep();
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
             params.put("fail", "Erreur pendant le calcul.");
             params.flash();
             validation.keep();
             add();
         }
         way.save();
 
         redirect("/way/list");
     }
 
     /**
      * [POST] Soumission d'édition de trajet
      * 
      * @param id
      * @param startCityId
      * @param finishCityId
      * @param cost
      * @param carId
      * @param placeAvailable
      * @param dateStart
      * @param hourStart
      * @param minCost
      * @param maxCost 
      */
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
 
     /**
      * [POST] Soumission d'une recherche de formulaire
      * 
      * @param startCityId
      * @param finishCityId
      * @param fromDate 
      */
     public static void searchResult(Long startCityId, Long finishCityId, @As("dd/MM/yyyy") Date fromDate) {
         models.City startCity = models.City.findById(startCityId);
         models.City finishCity = models.City.findById(finishCityId);
         List<models.Way> ways = new ArrayList<models.Way>();
         List<City> cities = City.find("order by name").fetch();
         
         if (startCity != null && finishCity != null) {
             for (models.User friend : User.connected().friends()) {
                 for (models.Way way : friend.ways_driver()) {
                     if (way.startCity == startCity && way.finishCity == finishCity && way.dateHourStart.getTime() >= fromDate.getTime()) {
                         ways.add(way);
                     }
                 }
             }
         }
 
         ways = _removeParticipationInArray(ways);
         render("way/search.html", ways, cities);
     }
 
     /**
      * [POST] Retourne le cout d'un trajet sous un format JSON
      * 
      * @param startCityId
      * @param finishCityId
      * @param carId
      * @throws Exception 
      */
     public static void calculCost(Long startCityId, Long finishCityId, Long carId) throws Exception {
         models.City startCity = models.City.findById(startCityId);
         models.City finishCity = models.City.findById(finishCityId);
         models.Car car = models.Car.findById(carId);
 
         List<Object> jsonReturn = new ArrayList<Object>();
         jsonReturn.add(new models.Way(startCity, finishCity, car).cost());
         jsonReturn.add(car.nbPlace);
 
         renderJSON(jsonReturn);
     }
 
     /**
      * Méthode supprimant tous les trajet ou l'utilisateur participe
      * 
      * @param ways
      * @return 
      */
     private static List<models.Way> _removeParticipationInArray(List<models.Way> ways) {
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
