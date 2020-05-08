 package com.Shows;
 
 import java.sql.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Random;
 
 import org.hibernate.Session;
 
 import com.Shows.Data.DataMapper.HibernateUtil;
 import com.Shows.Domain.Model.Entrada;
 import com.Shows.Domain.Model.Espectacle;
 import com.Shows.Domain.Model.Estat;
 import com.Shows.Domain.Model.Estrena;
 import com.Shows.Domain.Model.Local;
 import com.Shows.Domain.Model.Moneda;
 import com.Shows.Domain.Model.Representacio;
 import com.Shows.Domain.Model.Seient;
 import com.Shows.Domain.Model.SeientEnRepresentacio;
 import com.Shows.Domain.Model.Sessio;
 import com.Shows.Domain.Model.SetMoneda;
 import com.Shows.Domain.Model.ShowsCom;
 import com.Shows.Domain.Model.TipusSessio;
 
 public class DataBaseInitializer {
 	public static void initDatabase() {
 		Session session = HibernateUtil.getSession();
 		session.beginTransaction();
 
 		/* Locals */
 		Local local1 = new Local("Palau Sant Jordi", "C/Falsa 123");
		Local local2 = new Local("A Tomar Por Culo", "C/Parla");
 		Local local2_2 = new Local("PepeStage", "C/Tio Pepe");
 		Local local2_3 = new Local("Akihabara Playbox", "C/Japon 2222bis");
 		session.saveOrUpdate(local1);
 		session.saveOrUpdate(local2);
 		session.saveOrUpdate(local2_2);
 		session.saveOrUpdate(local2_3);
 
 		/* Seients */
 		int filas1 = 5;
 		int columnas1 = 5;
 		int filas2 = 3;
 		int columnas2 = 4;
 		int filas2_2 = 4;
 		int columnas2_2 = 6;
 		int filas2_3 = 2;
 		int columnas2_3 = 6;
 
 		HashSet<Seient> seients1 = new HashSet<Seient>();
 		HashSet<Seient> seients2 = new HashSet<Seient>();
 		HashSet<Seient> seients2_2 = new HashSet<Seient>();
 		HashSet<Seient> seients2_3 = new HashSet<Seient>();
 
 		for (int i = 0; i < filas1; ++i) {
 			for (int j = 0; j < columnas1; j++) {
 				Seient aux = new Seient(local1, j + 1, i + 1);
 				session.saveOrUpdate(aux);
 				seients1.add(aux);
 			}
 		}
 
 		for (int i = 0; i < filas2; ++i) {
 			for (int j = 0; j < columnas2; j++) {
 				Seient aux = new Seient(local2, j + 1, i + 1);
 				session.saveOrUpdate(aux);
 				seients2.add(aux);
 			}
 		}
 		for (int i = 0; i < filas2_2; ++i) {
 			for (int j = 0; j < columnas2_2; j++) {
 				Seient aux = new Seient(local2_2, j + 1, i + 1);
 				session.saveOrUpdate(aux);
 				seients2_2.add(aux);
 			}
 		}
 		for (int i = 0; i < filas2_3; ++i) {
 			for (int j = 0; j < columnas2_3; j++) {
 				Seient aux = new Seient(local2_3, j + 1, i + 1);
 				session.saveOrUpdate(aux);
 				seients2_3.add(aux);
 			}
 		}
 
 		local1.setSeients(seients1);
 		local2.setSeients(seients2);
 		local2_2.setSeients(seients2_2);
 		local2_3.setSeients(seients2_3);
 
 		/* Sessions */
 		Sessio ses1 = new Sessio(TipusSessio.tarda);
 		Sessio ses2 = new Sessio(TipusSessio.nit);
 		Sessio ses3 = new Sessio(TipusSessio.mati);
 		session.saveOrUpdate(ses1);
 		session.saveOrUpdate(ses2);
 		session.saveOrUpdate(ses3);
 
 		/* Data */
 		Date data1 = Date.valueOf("2012-06-30");
 		Date data2 = Date.valueOf("2012-07-31");
 		Date data3 = Date.valueOf("2012-06-20");
 		Date data4 = Date.valueOf("2012-06-19");
 
 		/* Representacions i Estrenes */
 		Float preu1 = 10f;
 		Float preu2 = 20f;
 
 		int lliures1 = filas1 * columnas1;
 		int lliures1_1 = filas1 * columnas1;
 		int lliures1_2 = filas1 * columnas1;
 		int lliures2 = filas2 * columnas2;
 		int lliures2_2_1 = filas2_2 * columnas2_2;
 		int lliures2_2_2 = filas2_2 * columnas2_2;
 		int lliures2_3 = filas2_3 * columnas2_3;
 		int lliures2_3_2 = filas2_3 * columnas2_3;
 		int lliures2_1 = filas2 * columnas2;
 
 		// tarda - PalauSantJordi - 30 juny
 		Representacio rep1 = new Representacio(ses1, local1, preu1, data1,
 				lliures1);
 		// nit - ATomarPorCulo - 31 juliol
 		Estrena est1 = new Estrena(ses2, local2, preu2, data2, lliures2, 5);
 		// tarda - PepeStage - 31 juliol
 		Estrena est2 = new Estrena(ses1, local2_2, preu2, data2, lliures2_2_1,
 				5);
 		// mati - Akihabara Playbox - 30 juny
 		Estrena est3 = new Estrena(ses3, local2_3, preu1, data1, lliures2_3_2,
 				7);
 		// nit - Akihabara Playbox - 31 juliol
 		Representacio rep2 = new Representacio(ses2, local2_3, preu2, data2,
 				lliures2_3);
 		// mati - PepeStage - 31 juliol
 		Representacio rep3 = new Representacio(ses3, local2_2, preu2, data2,
 				lliures2_2_2);
 		// nit - PalauSantJordi - 20 juny
 		Representacio rep4 = new Representacio(ses2, local1, preu1, data3,
 				lliures1_1);
 		// mati - PalauSantJordi - 20 juny
 		Estrena est4 = new Estrena(ses3, local1, preu1, data3, lliures1_2, 8);
 		// tarda - ATomarPorCulo - 19 juny
 		Estrena est5 = new Estrena(ses1, local2, preu2, data4, lliures2_1, 2);
 
 		session.saveOrUpdate(rep1);
 		session.saveOrUpdate(est1);
 		session.saveOrUpdate(est2);
 		session.saveOrUpdate(est3);
 		session.saveOrUpdate(rep2);
 		session.saveOrUpdate(rep3);
 		session.saveOrUpdate(rep4);
 		session.saveOrUpdate(est4);
 		session.saveOrUpdate(est5);
 
 		HashSet<Representacio> representacions1 = new HashSet<Representacio>();
 		representacions1.add(rep1);
 		representacions1.add(est3);
 		HashSet<Representacio> representacions2 = new HashSet<Representacio>();
 		representacions2.add(est1);
 		representacions2.add(est2);
 		representacions2.add(rep2);
 		representacions2.add(rep3);
 		representacions2.add(est5);
 		HashSet<Representacio> representacions3 = new HashSet<Representacio>();
 		representacions3.add(rep4);
 		representacions3.add(est4);
 
 		/* Seients en representacio */
 		seientsEnRepresentacio(rep1, seients1, lliures1, session);
 		seientsEnRepresentacio(est1, seients2, lliures2, session);
 		seientsEnRepresentacio(est2, seients2_2, lliures2_2_1, session);
 		seientsEnRepresentacio(rep2, seients2_3, lliures2_3, session);
 		seientsEnRepresentacio(rep3, seients2_2, lliures2_2_2, session);
 		seientsEnRepresentacio(est3, seients2_3, lliures2_3_2, session);
 		seientsEnRepresentacio(rep4, seients1, lliures1_1, session);
 		seientsEnRepresentacio(est4, seients1, lliures1_2, session);
 		seientsEnRepresentacio(est5, seients2, lliures2_1, session);
 
 		/* Espectacle */
 		Espectacle esp1 = new Espectacle("Espectacular", 3, representacions1);
 		Espectacle esp2 = new Espectacle("EspectaCULO", 1, representacions2);
 		Espectacle esp3 = new Espectacle("EspectoPatronum", 5, representacions3);
 		session.saveOrUpdate(esp1);
 		session.saveOrUpdate(esp2);
 		session.saveOrUpdate(esp3);
 
 		/* Entradas */
 		HashSet<Entrada> ent1 = new HashSet<Entrada>();
 		HashSet<Entrada> ent2 = new HashSet<Entrada>();
 		Entrada aux = new Entrada("1", "343434", 2, data1, preu1, rep1);
 		session.saveOrUpdate(aux);
 		ent1.add(aux);
 		aux = new Entrada("2", "343435", 2, data1, preu1, rep1);
 		session.saveOrUpdate(aux);
 		ent1.add(aux);
 		aux = new Entrada("3", "54545", 1, data2, preu2, est1);
 		session.saveOrUpdate(aux);
 		ent2.add(aux);
 
 		rep1.setEntradas(ent1);
 		est1.setEntradas(ent2);
 
 		/* Shows.com */
 
 		HashSet<Moneda> canvis = new HashSet<Moneda>();
 		canvis.add(Moneda.GBP);
 		canvis.add(Moneda.USD);
 
 		SetMoneda setMoneda = new SetMoneda(canvis);
 
 		ShowsCom showscom = new ShowsCom(1, 12121, "34343434", 3f, Moneda.EUR,
 				setMoneda);
 
 		session.saveOrUpdate(showscom);
 
 		session.getTransaction().commit();
 	}
 
 	private static void seientsEnRepresentacio(Representacio rep,
 			HashSet<Seient> seients, int lliures, Session session) {
 		Iterator<Seient> it = seients.iterator();
 		int ll = lliures;
 		Random randomGenerator = new Random();
 		HashSet<SeientEnRepresentacio> ser = new HashSet<SeientEnRepresentacio>();
 		while (it.hasNext()) {
 			int random = randomGenerator.nextInt(2);
 			if (random == 0) {
 				SeientEnRepresentacio aux = new SeientEnRepresentacio(
 						it.next(), rep, Estat.lliure);
 				session.saveOrUpdate(aux);
 				ser.add(aux);
 			} else {
 				SeientEnRepresentacio aux = new SeientEnRepresentacio(
 						it.next(), rep, Estat.ocupat);
 				session.saveOrUpdate(aux);
 				ser.add(aux);
 				--ll;
 			}
 		}
 		rep.setNombreSeientsLliures(ll);
 		rep.setSeientsEnRepresentacio(ser);
 	}
 }
