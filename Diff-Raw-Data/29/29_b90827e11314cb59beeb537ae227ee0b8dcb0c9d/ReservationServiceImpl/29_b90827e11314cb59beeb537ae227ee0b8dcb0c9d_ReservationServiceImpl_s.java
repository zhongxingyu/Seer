 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa165.jtravelagency.service;
 
 import cz.muni.fi.pa165.jtravelagency.dao.ReservationDAO;
 import cz.muni.fi.pa165.jtravelagency.dto.CustomerDTO;
 import cz.muni.fi.pa165.jtravelagency.dto.ReservationDTO;
 import cz.muni.fi.pa165.jtravelagency.dto.TripDTO;
 import cz.muni.fi.pa165.jtravelagency.entity.Customer;
 import cz.muni.fi.pa165.jtravelagency.entity.Reservation;
 import cz.muni.fi.pa165.jtravelagency.entity.Trip;
 import cz.muni.fi.pa165.jtravelagency.util.DTOAndEntityMapper;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.springframework.stereotype.Service;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author jakub
  */
 @Service
 @Transactional
 public class ReservationServiceImpl implements ReservationService {
     
     @Autowired
     private ReservationDAO reservationDAO;
 
     public void setReservationDAO(ReservationDAO reservationDAO) {
         this.reservationDAO = reservationDAO;
     }
     
     public void create(ReservationDTO reservationDTO){
         if (reservationDTO == null) {
             throw new IllegalArgumentException("ReservationDTO is null.");
         }
         if (reservationDTO.getId() != null) {
             throw new IllegalArgumentException("ReservationDTO's id is null.");
         }
         Reservation reservation=DTOAndEntityMapper.dtoToEntity(reservationDTO, Reservation.class);
         reservationDAO.createReservation(reservation);
         reservationDTO.setId(reservation.getId());
         
         for(int i=0;i<reservationDTO.getExcursions().size();i++){
             reservationDTO.getExcursions().get(i).setId(reservation.getExcursions().get(i).getId());
         }
         reservationDTO.getTrip().setId(reservation.getTrip().getId());
         //ceknut to proti null pripadne
         reservationDTO.getCustomer().setId(reservation.getCustomer().getId());
         //CEKNUT KVOLI NULL-LU
     }
     public void delete(ReservationDTO  reservationDTO){
         if (reservationDTO == null) {
             throw new IllegalArgumentException("ReservationDTO is null.");
         }
         if (reservationDTO.getId() != null) {
             throw new IllegalArgumentException("ReservationDTO's id is null.");
         }
         Reservation reservation=DTOAndEntityMapper.dtoToEntity(reservationDTO, Reservation.class);
         reservationDAO.deleteReservation(reservation);
     }
     
     public void update(ReservationDTO  reservationDTO){
         if (reservationDTO == null) {
             throw new IllegalArgumentException("ReservationDTO is null.");
         }
         if (reservationDTO.getId() != null) {
             throw new IllegalArgumentException("ReservationDTO's id is null.");
         }
         
         Reservation reservation=DTOAndEntityMapper.dtoToEntity(reservationDTO, Reservation.class);
         reservationDAO.updateReservation(reservation);
         
         reservationDTO=DTOAndEntityMapper.entityToDto(reservation, ReservationDTO.class);
     }
     public ReservationDTO  get(Long id){
         Reservation reservation=reservationDAO.getReservation(id);
         return DTOAndEntityMapper.entityToDto(reservation, ReservationDTO.class);
     }
     public List<ReservationDTO> getAll(){
         List<Reservation> reservations=new ArrayList<Reservation>();
         reservations=reservationDAO.getAllReservations();
         List<ReservationDTO> reservationsDTO=new ArrayList<ReservationDTO>();
         for(Reservation r: reservations){
             reservationsDTO.add(DTOAndEntityMapper.entityToDto(r, ReservationDTO.class));
         }
         return reservationsDTO;
     }
     
     public List<ReservationDTO> getByTrip(TripDTO trip){
         List<Reservation> reservations=new ArrayList<Reservation>();
         reservations=reservationDAO.getReservationByTrip(DTOAndEntityMapper.dtoToEntity(trip, Trip.class));
         List<ReservationDTO> reservationsDTO=new ArrayList<ReservationDTO>();
         for(Reservation r: reservations){
             reservationsDTO.add(DTOAndEntityMapper.entityToDto(r, ReservationDTO.class));
         }
         return reservationsDTO;
     }
     
     public List<ReservationDTO> getByCustomer(CustomerDTO customer){
         List<Reservation> reservations=new ArrayList<Reservation>();
         reservations=reservationDAO.getReservationByCustomer(DTOAndEntityMapper.dtoToEntity(customer, Customer.class));
         List<ReservationDTO> reservationsDTO=new ArrayList<ReservationDTO>();
         for(Reservation r: reservations) {
             reservationsDTO.add(DTOAndEntityMapper.entityToDto(r, ReservationDTO.class));
         }
         return reservationsDTO;
     }
     
 
     
 
     
 }
