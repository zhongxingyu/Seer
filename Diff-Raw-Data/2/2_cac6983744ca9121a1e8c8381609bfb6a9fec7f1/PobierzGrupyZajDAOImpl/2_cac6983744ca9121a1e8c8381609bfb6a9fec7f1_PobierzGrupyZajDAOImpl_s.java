 package com.project.dao;
 
 import java.util.List;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.project.data.GrupyProjektowe;
 import com.project.data.GrupyZajeciowe;
 import com.project.data.Notatki;
 import com.project.data.Obecnosc;
 import com.project.data.OcenyCzastkowe;
 import com.project.data.Spotkania;
 import com.project.data.Studenci;
 import com.project.data.StudenciDoGrupProjektowych;
 import com.project.data.StudenciDoGrupZajeciowych;
 
 
 @Repository
 public class PobierzGrupyZajDAOImpl implements PobierzGrupyZajDAO  {
 
 	
 
 		@Autowired
 		private SessionFactory sessionFactory;
 		
 		@Transactional
 		public List<GrupyZajeciowe> pobierzGrupyZajeciowe(int idGrupy) {
 			return sessionFactory
 					.getCurrentSession()
 					.createQuery(
 							"from GrupyZajeciowe where idGrupyZajeciowe =:idGrupy")
 					.setInteger("idGrupy", idGrupy).list();
 		}
 		
 		@Transactional
 		public List<GrupyProjektowe> pobierzGrupyProjektowe(int idGrupy) {
 			return sessionFactory
 					.getCurrentSession()
 					.createQuery(
 							"from GrupyProjektowe where idGrupyZajeciowe =:idGrupy")
 					.setInteger("idGrupy", idGrupy).list();
 		}
 		
 		@Transactional
 		public List<StudenciDoGrupProjektowych> pobierzStudentowZgrupy(int idGrupyProjektowej) {
 			return sessionFactory
 					.getCurrentSession()
 					.createQuery(
 							"from StudenciDoGrupProjektowych where idGrupyProjektowej =:idGrupyProjektowej")
 					.setInteger("idGrupyProjektowej", idGrupyProjektowej).list();
 		}
 		
 		@Transactional
 		public List<Studenci> pobierzStudentow(int idStudenci) {
 			return sessionFactory
 					.getCurrentSession()
 					.createQuery(
 							"from Studenci where idStudenci =:idStudenci")
 					.setInteger("idStudenci", idStudenci).list();
 		}
 		
 		@Transactional
 		public List<Spotkania> pobierzSpotkania(int idGrupyZajeciowe) {
 			return sessionFactory
 					.getCurrentSession()
 					.createQuery(
 							"from Spotkania where idGrupyProjektowej =:idGrupyZajeciowe")
 					.setInteger("idGrupyZajeciowe", idGrupyZajeciowe).list();
 		}
 		
 		@Transactional
 		public List<OcenyCzastkowe> pobierzOcenyCzastkowe(int idSpotkania, int idStudenta) {
 			return sessionFactory
 					.getCurrentSession()
 					.createQuery(
 							"from OcenyCzastkowe where idSpotkania =:idSpotkania and idStudenta =:idStudenta")
 					.setInteger("idSpotkania", idSpotkania).setInteger("idStudenta", idStudenta).list();
 		}
 		
 		@Transactional
 		public List<Obecnosc> pobierzObecnosci(int idSpotkania, int idStudenta) {
 			return sessionFactory
 					.getCurrentSession()
 					.createQuery(
 							"from Obecnosc where idSpotkania =:idSpotkania and idStudenta =:idStudenta")
 					.setInteger("idSpotkania", idSpotkania).setInteger("idStudenta", idStudenta).list();
 		}
 		
 		
 		@Transactional
 		public List<StudenciDoGrupZajeciowych> pobierzStudGrup(int idGrupyChodzacej) {
 			return sessionFactory
 					.getCurrentSession()
 					.createQuery(
 							"from StudenciDoGrupZajeciowych where idGrupyChodzacej =:idGrupyChodzacej")
 					.setInteger("idGrupyChodzacej", idGrupyChodzacej).list();
 		}
 		
 		@Transactional
 		public List<Notatki> pobierzNotatki(int idGrupyProjektowej) {
 			return sessionFactory
 					.getCurrentSession()
 					.createQuery(
 							"from Notatki where idGrupyProjektowej =:idGrupyProjektowej")
					.setInteger("idGrupyChodzacej", idGrupyProjektowej).list();
 		}
 }
