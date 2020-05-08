 /*
  * Copyright (C) 2012 AXIA Studio (http://www.axiastudio.com)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.axiastudio.suite.demo;
 
 import com.axiastudio.pypapi.Register;
 import com.axiastudio.pypapi.db.Database;
 import com.axiastudio.pypapi.db.IDatabase;
 import com.axiastudio.suite.Suite;
 import com.axiastudio.suite.SuiteUtil;
 import com.axiastudio.suite.anagrafiche.entities.SessoSoggetto;
 import com.axiastudio.suite.anagrafiche.entities.Soggetto;
 import com.axiastudio.suite.anagrafiche.entities.TipoSoggetto;
 import com.axiastudio.suite.base.entities.IUtente;
 import com.axiastudio.suite.base.entities.Ufficio;
 import com.axiastudio.suite.base.entities.UfficioUtente;
 import com.axiastudio.suite.base.entities.Utente;
 import com.axiastudio.suite.deliberedetermine.entities.Determina;
 import com.axiastudio.suite.deliberedetermine.entities.ServizioDetermina;
 import com.axiastudio.suite.finanziaria.entities.Capitolo;
 import com.axiastudio.suite.finanziaria.entities.Servizio;
 import com.axiastudio.suite.pratiche.PraticaCallbacks;
 import com.axiastudio.suite.pratiche.entities.Pratica;
 import com.axiastudio.suite.pratiche.entities.TipoPratica;
 import com.axiastudio.suite.procedimenti.entities.Carica;
 import com.axiastudio.suite.procedimenti.entities.CodiceCarica;
 import com.axiastudio.suite.procedimenti.entities.Delega;
 import com.axiastudio.suite.procedimenti.entities.Iniziativa;
 import com.axiastudio.suite.procedimenti.entities.Norma;
 import com.axiastudio.suite.procedimenti.entities.NormaProcedimento;
 import com.axiastudio.suite.procedimenti.entities.Procedimento;
 import com.axiastudio.suite.procedimenti.entities.TipoNorma;
 import com.axiastudio.suite.procedimenti.entities.TipoPraticaProcedimento;
 import com.axiastudio.suite.protocollo.ProtocolloCallbacks;
 import com.axiastudio.suite.protocollo.entities.*;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 
 /**
  *
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  */
 public class DemoData {
     
     public static void main(String[] args) {
         DemoData.initData();
     }
     
     public DemoData() {
     }
     
     public static void initSchema(){
         // inizializza gli schemi
         List<String> schema = new ArrayList();
         schema.add("BASE");
         schema.add("ANAGRAFICHE");
         schema.add("PROTOCOLLO");
         schema.add("PUBBLICAZIONI");
         schema.add("PROCEDIMENTI");
         schema.add("PRATICHE");
         schema.add("SEDUTE");
         schema.add("FINANZIARIA");
         schema.add("DELIBEREDETERMINE");
         schema.add("GENERALE");
         for( String name: schema){
             try {
                 Connection conn = DriverManager.getConnection("jdbc:h2:~/suite","","");
                 Statement st = conn.createStatement();
                 st.executeUpdate("DROP SCHEMA IF EXISTS " + name + ";");
                 st.executeUpdate("CREATE SCHEMA " + name + ";");
                 st.close();
                 conn.close();
             } catch (SQLException ex) {
                 Logger.getLogger(Suite.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
     }
     
     public static void initData(){
         // inizializzo e apro la transazione
         Database db = (Database) Register.queryUtility(IDatabase.class);
         EntityManagerFactory emf = db.getEntityManagerFactory();
         EntityManager em = emf.createEntityManager();
         //em.getTransaction().begin();
         
 
         /* 
          * AMMINISTRATORE
          */
         Utente admin = new Utente();
         admin.setLogin("admin");
         admin.setNome("Amministratore");
         admin.setPassword(SuiteUtil.digest("pypapi"));
         admin.setAmministratore(Boolean.TRUE);
         em.getTransaction().begin();
         em.persist(admin);
         em.getTransaction().commit();
         
         /*
          * TITOLI (soggetto-protocollo)
          */
         Titolo proprietario = new Titolo();
         proprietario.setDescrizione("PROPRIETARIO");
         proprietario.setTipo(TipoTitolo.PERSONA_INTERESSATA);
         Titolo progettista = new Titolo();
         progettista.setDescrizione("PROGETTISTA");
         progettista.setTipo(TipoTitolo.TECNICO);
         em.getTransaction().begin();
         em.persist(proprietario);
         em.persist(progettista);
         em.getTransaction().commit();
         
         /*
          * CARICHE
          */
         Carica sindaco = new Carica();
         sindaco.setDescrizione("Sindaco");
         sindaco.setCodiceCarica(CodiceCarica.SINDACO);
         Carica viceSindaco = new Carica();
         viceSindaco.setDescrizione("Vice Sindaco");
         viceSindaco.setCodiceCarica(CodiceCarica.VICE_SINDACO);
         Carica segretario = new Carica();
         segretario.setDescrizione("Segretario");
         segretario.setCodiceCarica(CodiceCarica.SEGRETARIO);
         Carica responsabile = new Carica();
         responsabile.setDescrizione("Responsabile del servizio di bilancio");
         responsabile.setCodiceCarica(CodiceCarica.RESPONSABILE_DI_SERVIZIO);
         em.getTransaction().begin();
         em.persist(sindaco);
         em.persist(viceSindaco);
         em.persist(segretario);
         em.persist(responsabile);
         em.getTransaction().commit();
         
         /*
          * SERVIZI
          */
         Servizio servizioAffariGenerali = new Servizio();
         servizioAffariGenerali.setDescrizione("Servizio affari generali");
         Servizio servizioInformativoComunale = new Servizio();
         servizioInformativoComunale.setDescrizione("Servizio informativo comunale");
         em.getTransaction().begin();
         em.persist(servizioAffariGenerali);
         em.persist(servizioInformativoComunale);
         em.getTransaction().commit();
 
         /*
          * UFFICI
          */
         Ufficio uffInf = new Ufficio();
         uffInf.setDescrizione("Ufficio informativo");
         uffInf.setSportello(Boolean.TRUE);
         uffInf.setMittenteodestinatario(Boolean.TRUE);
         uffInf.setAttribuzione(Boolean.TRUE);
         Ufficio uffPro = new Ufficio();
         uffPro.setSportello(Boolean.TRUE);
         uffPro.setMittenteodestinatario(Boolean.FALSE);
         uffPro.setAttribuzione(Boolean.TRUE);
         uffPro.setDescrizione("Ufficio protocollo");
         Ufficio uffEdi = new Ufficio();
         uffEdi.setDescrizione("Ufficio edilizia");
         uffEdi.setSportello(Boolean.FALSE);
         uffEdi.setMittenteodestinatario(Boolean.TRUE);
         uffEdi.setAttribuzione(Boolean.TRUE);
         Ufficio uffCom = new Ufficio();
         uffCom.setSportello(Boolean.FALSE);
         uffCom.setMittenteodestinatario(Boolean.TRUE);
         uffCom.setAttribuzione(Boolean.TRUE);
         uffCom.setDescrizione("Ufficio commercio");
         em.getTransaction().begin();
         em.persist(uffInf);
         em.persist(uffPro);
         em.persist(uffEdi);
         em.persist(uffCom);
         em.getTransaction().commit();
         
         /*
          * UTENTI (mario è l'utente autenticato)
          * 
          * Luigi è segretario
          * Mario è responsabile di servizio informativo
          * 
          * Mario ha anche la delega come segretario da Luigi
          * 
          */
         Utente luigi = new Utente();
         luigi.setLogin("luigi");
         luigi.setNome("Luigi Bros");
         luigi.setSigla("L.B.");
         luigi.setPassword(SuiteUtil.digest("bros"));
         List<Delega> delegheLuigi = new ArrayList();
         Delega titolareSegretario = new Delega();
         titolareSegretario.setUtente(luigi);
         titolareSegretario.setCarica(segretario);
         titolareSegretario.setTitolare(Boolean.TRUE);
         delegheLuigi.add(titolareSegretario);
         luigi.setDelegaCollection(delegheLuigi);
         em.getTransaction().begin();
         em.persist(luigi);
         em.getTransaction().commit();
         
         Utente mario = new Utente();
         mario.setLogin("mario");
         mario.setNome("Mario Super");
         mario.setSigla("M.S.");
         mario.setPassword(SuiteUtil.digest("super"));
         mario.setOperatoreprotocollo(Boolean.TRUE);
         mario.setOperatoreanagrafiche(Boolean.TRUE);
         mario.setOperatorepratiche(Boolean.TRUE);
         Register.registerUtility(mario, IUtente.class);
         List<UfficioUtente> ufficiUtente = new ArrayList();
         UfficioUtente uu = new UfficioUtente();
         uu.setUfficio(uffInf);
         uu.setRicerca(Boolean.TRUE);
         uu.setVisualizza(Boolean.TRUE);
         ufficiUtente.add(uu);
         UfficioUtente uu2 = new UfficioUtente();
         uu2.setUfficio(uffPro);
         uu2.setRicerca(Boolean.TRUE);
         ufficiUtente.add(uu2);
         mario.setUfficioUtenteCollection(ufficiUtente);
         List<Delega> delegheMario = new ArrayList();
         Delega titolareResponsabile = new Delega();
         titolareResponsabile.setUtente(mario);
         titolareResponsabile.setCarica(responsabile);
         titolareResponsabile.setServizio(servizioInformativoComunale);
         titolareResponsabile.setTitolare(Boolean.TRUE);
         delegheMario.add(titolareResponsabile);
         Delega delegaSegretario = new Delega();
         delegaSegretario.setUtente(mario);
         delegaSegretario.setDelegante(luigi);
         delegaSegretario.setCarica(segretario);
         delegaSegretario.setDelegato(Boolean.TRUE);
         delegheMario.add(delegaSegretario);
         mario.setDelegaCollection(delegheMario);
         em.getTransaction().begin();
         em.persist(mario);
         em.getTransaction().commit();
         
         /*
          * SOGGETTI
          */
         Soggetto tiziano = new Soggetto();
         tiziano.setNome("Tiziano");
         tiziano.setCognome("Lattisi");
         tiziano.setSessosoggetto(SessoSoggetto.M);
         tiziano.setTipo(TipoSoggetto.PERSONA);
         em.getTransaction().begin();
         em.persist(tiziano);
         em.getTransaction().commit();
         
         /*
          * TIPI DI PRATICA
          */
         TipoPratica det = new TipoPratica();
         det.setCodice("DET");
         det.setDescrizione("Determina");
         TipoPratica detrs = new TipoPratica();
         detrs.setCodice("DETRS");
         detrs.setDescrizione("Determina del responsabile");
         detrs.setTipopadre(det);
         em.getTransaction().begin();
         em.persist(det);
         em.persist(detrs);
         em.getTransaction().commit();
         
         /*
          * NORME
          */
         Norma norma1 = new Norma();
         norma1.setTipo(TipoNorma.COMUNALE);
         norma1.setDescrizione("Esempio di norma comunale");
         Norma norma2 = new Norma();
         norma2.setTipo(TipoNorma.REGIONALE);
         norma2.setDescrizione("Esempio di norma regionale");
         em.getTransaction().begin();
         em.persist(norma1);
         em.persist(norma2);
         em.getTransaction().commit();
 
         /*
          * PROCEDIMENTI
          */
         Procedimento procedimento = new Procedimento();
         procedimento.setDescrizione("Determina del responsabile del servizio");
         procedimento.setMaxGiorniIstruttoria(30);
         procedimento.setIniziativa(Iniziativa.DI_UFFICIO);
         procedimento.setNormativa("Descrizione generica della normativa");
         procedimento.setAttivo(Boolean.TRUE);
         List<NormaProcedimento> norme = new ArrayList();
         NormaProcedimento norma1Procedimento = new NormaProcedimento();
         norma1Procedimento.setNorma(norma1);
         norme.add(norma1Procedimento);
         NormaProcedimento norma2Procedimento = new NormaProcedimento();
         norma2Procedimento.setNorma(norma2);
         norme.add(norma2Procedimento);
         procedimento.setNormaProcedimentoCollection(norme);
         List<TipoPraticaProcedimento> tipiPratica = new ArrayList();
         TipoPraticaProcedimento detrsProcedimento = new TipoPraticaProcedimento();
         detrsProcedimento.setTipopratica(detrs);
         tipiPratica.add(detrsProcedimento);
         procedimento.setTipopraticaProcedimentoCollection(tipiPratica);
         em.getTransaction().begin();
         em.persist(procedimento);
         em.getTransaction().commit();
         
         // pratiche
         Pratica pratica = new Pratica();
         pratica.setDescrizione("Pratica demo");
         pratica.setTipo(detrs);
         PraticaCallbacks.validaPratica(pratica);
         em.getTransaction().begin();
         em.persist(pratica);
         em.getTransaction().commit();
         
         // protocolli
         Protocollo pro1 = new Protocollo();
         pro1.setOggetto("Oggetto del protocollo");
         pro1.setNote("Note del protocollo");
         pro1.setTipo(TipoProtocollo.ENTRATA);
         pro1.setSportello(uffInf);
         pro1.setRichiederisposta(Boolean.TRUE);
         UfficioProtocollo up = new UfficioProtocollo();
         up.setUfficio(uffInf);
         up.setProtocollo(pro1);
         List<UfficioProtocollo> ufficiprotocollo = new ArrayList<UfficioProtocollo>();
         ufficiprotocollo.add(up);
         pro1.setUfficioProtocolloCollection(ufficiprotocollo);
         List<Attribuzione> attribuzioni = new ArrayList<Attribuzione>();
         Attribuzione a1 = new Attribuzione();
         a1.setUfficio(uffInf);
         a1.setProtocollo(pro1);
         Attribuzione a2 = new Attribuzione();
         a2.setUfficio(uffPro);
         a2.setProtocollo(pro1);
         attribuzioni.add(a1);
         attribuzioni.add(a2);
         pro1.setAttribuzioneCollection(attribuzioni);
         SoggettoProtocollo sp = new SoggettoProtocollo();
         sp.setSoggetto(tiziano);
         sp.setTitolo(proprietario);
         sp.setProtocollo(pro1);
         List<SoggettoProtocollo> soggettiprotocollo = new ArrayList<SoggettoProtocollo>();
         soggettiprotocollo.add(sp);
         pro1.setSoggettoProtocolloCollection(soggettiprotocollo);
         PraticaProtocollo pp = new PraticaProtocollo();
         pp.setPratica(pratica);
         pp.setProtocollo(pro1);
         List<PraticaProtocollo> praticheprotocollo = new ArrayList<PraticaProtocollo>();
         praticheprotocollo.add(pp);
         pro1.setPraticaProtocolloCollection(praticheprotocollo);            
         Protocollo pro2 = new Protocollo();
         pro2.setOggetto("Oggetto del protocollo2");
         pro2.setNote("Note del protocollo2");
         pro2.setSportello(uffPro);
         pro2.setTipo(TipoProtocollo.USCITA);
         SoggettoProtocollo sp2 = new SoggettoProtocollo();
         sp2.setSoggetto(tiziano);
         sp2.setTitolo(progettista);
         sp2.setProtocollo(pro2);
         List<SoggettoProtocollo> soggettiprotocollo2 = new ArrayList<SoggettoProtocollo>();
         soggettiprotocollo2.add(sp2);
         pro2.setSoggettoProtocolloCollection(soggettiprotocollo2);
         
         ProtocolloCallbacks.beforeCommit(pro1);
         em.getTransaction().begin();
         em.persist(pro1);
         em.getTransaction().commit();
         
         ProtocolloCallbacks.beforeCommit(pro2);
         em.getTransaction().begin();
         em.persist(pro2);
         em.getTransaction().commit();
         
         
         // Capitoli
         Capitolo c1 = new Capitolo();
         c1.setDescrizione("Capitolo 1");
         Capitolo c2 = new Capitolo();
         c2.setDescrizione("Capitolo 2");
 
         // Determina
         Determina determina = new Determina();
         determina.setOggetto("Determina di test");
         List<ServizioDetermina> servizioDeterminaCollection = new ArrayList();
         ServizioDetermina servizioDetermina = new ServizioDetermina();
         servizioDetermina.setServizio(servizioAffariGenerali);
         servizioDeterminaCollection.add(servizioDetermina);
         determina.setServizioDeterminaCollection(servizioDeterminaCollection);
         determina.setIdpratica(pratica.getIdpratica());
        determina.setCodiceinterno("DETRS201300001");
         
         em.getTransaction().begin();
         em.persist(servizioAffariGenerali);
         em.persist(servizioInformativoComunale);
         em.persist(c1);
         em.persist(c2);
         em.persist(determina);
         em.getTransaction().commit();
         
     }
 }
