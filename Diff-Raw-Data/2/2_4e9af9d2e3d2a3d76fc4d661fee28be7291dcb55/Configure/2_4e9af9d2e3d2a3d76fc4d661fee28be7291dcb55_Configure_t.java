 /*
  * Copyright (C) 2013 AXIA Studio (http://www.axiastudio.com)
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
  * You should have received a copy of the GNU Afffero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.axiastudio.suite;
 
 import com.axiastudio.pypapi.Application;
 import com.axiastudio.pypapi.IStreamProvider;
 import com.axiastudio.pypapi.Register;
 import com.axiastudio.pypapi.Resolver;
 import com.axiastudio.pypapi.db.Database;
 import com.axiastudio.suite.modelli.entities.Modello;
 import com.axiastudio.suite.modelli.entities.Segnalibro;
 import com.axiastudio.suite.plugins.cmis.CmisPlugin;
 import com.axiastudio.suite.plugins.cmis.CmisStreamProvider;
 import com.axiastudio.suite.plugins.ooops.FileStreamProvider;
 import com.axiastudio.suite.plugins.ooops.OoopsPlugin;
 import com.axiastudio.suite.plugins.ooops.RuleSet;
 import com.axiastudio.suite.plugins.ooops.Template;
 import com.axiastudio.pypapi.ui.Dialog;
 import com.axiastudio.pypapi.ui.IQuickInsertDialog;
 import com.axiastudio.pypapi.ui.Window;
 import com.axiastudio.suite.anagrafiche.AnagraficheAdapters;
 import com.axiastudio.suite.anagrafiche.entities.AlboProfessionale;
 import com.axiastudio.suite.anagrafiche.entities.Gruppo;
 import com.axiastudio.suite.anagrafiche.entities.GruppoSoggetto;
 import com.axiastudio.suite.anagrafiche.entities.Indirizzo;
 import com.axiastudio.suite.anagrafiche.entities.Relazione;
 import com.axiastudio.suite.anagrafiche.entities.RelazioneSoggetto;
 import com.axiastudio.suite.anagrafiche.entities.Riferimento;
 import com.axiastudio.suite.anagrafiche.entities.Soggetto;
 import com.axiastudio.suite.anagrafiche.entities.Stato;
 import com.axiastudio.suite.anagrafiche.entities.TitoloSoggetto;
 import com.axiastudio.suite.anagrafiche.entities.TitoloStudio;
 import com.axiastudio.suite.anagrafiche.entities.TitoloStudioSoggetto;
 import com.axiastudio.suite.anagrafiche.forms.FormIndirizzo;
 import com.axiastudio.suite.anagrafiche.forms.FormQuickInsertSoggetto;
 import com.axiastudio.suite.anagrafiche.forms.FormRelazioneSoggetto;
 import com.axiastudio.suite.anagrafiche.forms.FormSoggetto;
 import com.axiastudio.suite.base.entities.Giunta;
 import com.axiastudio.suite.base.entities.Ufficio;
 import com.axiastudio.suite.base.entities.Utente;
 import com.axiastudio.suite.deliberedetermine.entities.Determina;
 import com.axiastudio.suite.deliberedetermine.entities.MovimentoDetermina;
 import com.axiastudio.suite.deliberedetermine.forms.FormDetermina;
 import com.axiastudio.suite.finanziaria.entities.Capitolo;
 import com.axiastudio.suite.finanziaria.entities.Servizio;
 import com.axiastudio.suite.generale.entities.Costante;
 import com.axiastudio.suite.generale.entities.Etichetta;
 import com.axiastudio.suite.pratiche.PraticaAdapters;
 import com.axiastudio.suite.pratiche.PraticaCallbacks;
 import com.axiastudio.suite.pratiche.PraticaPrivate;
 import com.axiastudio.suite.pratiche.entities.Dipendenza;
 import com.axiastudio.suite.pratiche.entities.DipendenzaPratica;
 import com.axiastudio.suite.pratiche.entities.Pratica;
 import com.axiastudio.suite.pratiche.entities.TipoPratica;
 import com.axiastudio.suite.pratiche.entities.Fase;
 import com.axiastudio.suite.pratiche.forms.FormDipendenzaPratica;
 import com.axiastudio.suite.pratiche.forms.FormPratica;
 import com.axiastudio.suite.procedimenti.GestoreDeleghe;
 import com.axiastudio.suite.procedimenti.IGestoreDeleghe;
 import com.axiastudio.suite.procedimenti.entities.*;
 import com.axiastudio.suite.procedimenti.forms.FormDelega;
 import com.axiastudio.suite.procedimenti.forms.FormFaseProcedimento;
 import com.axiastudio.suite.protocollo.ProtocolloAdapters;
 import com.axiastudio.suite.protocollo.ProtocolloCallbacks;
 import com.axiastudio.suite.protocollo.ProtocolloPrivate;
 import com.axiastudio.suite.protocollo.entities.AnnullamentoProtocollo;
 import com.axiastudio.suite.protocollo.entities.Fascicolo;
 import com.axiastudio.suite.protocollo.entities.MotivazioneAnnullamento;
 import com.axiastudio.suite.protocollo.entities.Oggetto;
 import com.axiastudio.suite.protocollo.entities.PraticaProtocollo;
 import com.axiastudio.suite.protocollo.entities.Protocollo;
 import com.axiastudio.suite.protocollo.entities.SoggettoProtocollo;
 import com.axiastudio.suite.protocollo.entities.SoggettoRiservatoProtocollo;
 import com.axiastudio.suite.protocollo.entities.Titolo;
 import com.axiastudio.suite.protocollo.forms.FormAnnullamentoProtocollo;
 import com.axiastudio.suite.protocollo.forms.FormProtocollo;
 import com.axiastudio.suite.protocollo.forms.FormScrivania;
 import com.axiastudio.suite.protocollo.forms.FormSoggettoProtocollo;
 import com.axiastudio.suite.protocollo.forms.FormPraticaProtocollo;
 import com.axiastudio.suite.pubblicazioni.entities.Pubblicazione;
 import com.axiastudio.suite.pubblicazioni.forms.FormPubblicazione;
 import com.axiastudio.suite.richieste.RichiestaCallbacks;
 import com.axiastudio.suite.richieste.entities.DestinatarioUfficio;
 import com.axiastudio.suite.richieste.entities.Richiesta;
 import com.axiastudio.suite.richieste.forms.FormRichiesta;
 import com.axiastudio.suite.sedute.entities.CaricaCommissione;
 import com.axiastudio.suite.sedute.entities.Commissione;
 import com.axiastudio.suite.sedute.entities.Seduta;
 import com.axiastudio.suite.sedute.entities.TipoSeduta;
 import com.axiastudio.suite.sedute.forms.FormTipoSeduta;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 
 /**
  *
  * @author AXIA Studio (http://www.axiastudio.com)
  */
 public class Configure {
 
     private static Database db;
     
     public static void configure(Database db, Properties properties){
 
         db = db;
         adapters();
         callbacks();
         privates();
 
         forms(db);
         plugins(properties);
         templates(properties);
 
         // gestore deleghe
         GestoreDeleghe gestoreDeleghe = new GestoreDeleghe();
         Register.registerUtility(gestoreDeleghe, IGestoreDeleghe.class);
 
     }
 
     private static void adapters() {
         Register.registerAdapters(Resolver.adaptersFromClass(AnagraficheAdapters.class));        
         Register.registerAdapters(Resolver.adaptersFromClass(ProtocolloAdapters.class));
         Register.registerAdapters(Resolver.adaptersFromClass(PraticaAdapters.class));
     }
 
     private static void callbacks() {
         Register.registerCallbacks(Resolver.callbacksFromClass(ProtocolloCallbacks.class));
         Register.registerCallbacks(Resolver.callbacksFromClass(PraticaCallbacks.class));
         Register.registerCallbacks(Resolver.callbacksFromClass(RichiestaCallbacks.class));
     }
 
     private static void privates() {
         Register.registerPrivates(Resolver.privatesFromClass(PraticaPrivate.class));
         Register.registerPrivates(Resolver.privatesFromClass(ProtocolloPrivate.class));
     }
 
     private static void plugins(Properties properties) {
 
         /* CMIS */
 
         String cmisUrl = properties.getProperty("cmis.url");
         String cmisUser = properties.getProperty("cmis.user");
         String cmisPassword = properties.getProperty("cmis.password");
         Application app = Application.getApplicationInstance();
         String alfrescoPathProtocollo = (String) app.getConfigItem("alfrescopath.protocollo");
         String alfrescoPathPratica = (String) app.getConfigItem("alfrescopath.pratica");
         String alfrescoPathRichiesta = (String) app.getConfigItem("alfrescopath.richiesta");
         String alfrescoPathPubblicazione = (String) app.getConfigItem("alfrescopath.pubblicazione");
         String ooopsConnString = (String) app.getConfigItem("ooops.connection");
 
         CmisPlugin cmisPlugin = new CmisPlugin();
         String templateCmisProtocollo = alfrescoPathProtocollo + "/${dataprotocollo,date,yyyy}/${dataprotocollo,date,MM}/${dataprotocollo,date,dd}/${iddocumento}/";
         cmisPlugin.setup(cmisUrl, cmisUser, cmisPassword,
                 templateCmisProtocollo,
                 Boolean.FALSE);
         Register.registerPlugin(cmisPlugin, FormProtocollo.class);
         Register.registerPlugin(cmisPlugin, FormScrivania.class);
 
         CmisPlugin cmisPluginPubblicazioni = new CmisPlugin();
         cmisPluginPubblicazioni.setup(cmisUrl, cmisUser, cmisPassword,
                 alfrescoPathPubblicazione + "/${inizioconsultazione,date,yyyy}/${inizioconsultazione,date,MM}/${inizioconsultazione,date,dd}/${id}/");
         Register.registerPlugin(cmisPluginPubblicazioni, FormPubblicazione.class);
 
         CmisPlugin cmisPluginPratica = new CmisPlugin();
         cmisPluginPratica.setup(cmisUrl, cmisUser, cmisPassword,
                 alfrescoPathPratica + "/${datapratica,date,yyyy}/${datapratica,date,MM}/${idpratica}/");
         Register.registerPlugin(cmisPluginPratica, FormPratica.class);
         Register.registerPlugin(cmisPluginPratica, FormDetermina.class);
 
         CmisPlugin cmisPluginRichiesta = new CmisPlugin();
         cmisPluginRichiesta.setup(cmisUrl, cmisUser, cmisPassword,
                 alfrescoPathRichiesta + "/${data,date,yyyy}/${data,date,MM}/${id}/");
         Register.registerPlugin(cmisPluginRichiesta, FormRichiesta.class);
 
         /* OOOPS (OpenOffice) */
 
         OoopsPlugin ooopsPlugin = new OoopsPlugin();
         ooopsPlugin.setup(ooopsConnString);
         Register.registerPlugin(ooopsPlugin, FormPratica.class);
         Register.registerPlugin(ooopsPlugin, FormDetermina.class);
 
     }
 
     private static void templates(Properties properties) {
 
         /* CMIS */
         String cmisUrl = properties.getProperty("cmis.url");
         String cmisUser = properties.getProperty("cmis.user");
         String cmisPassword = properties.getProperty("cmis.password");
 
         OoopsPlugin ooopsPlugin = (OoopsPlugin) Register.queryPlugin(FormPratica.class, "Ooops");
         List<Modello> modelli = SuiteUtil.elencoModelli();
         for( Modello modello: modelli ){
             HashMap<String,String> map = new HashMap<String, String>();
             for( Segnalibro segnalibro: modello.getSegnalibroCollection() ){
                 map.put(segnalibro.getSegnalibro(), segnalibro.getCodice());
             }
             RuleSet ruleSet = new RuleSet(map);
             IStreamProvider streamProvider = null;
            if( modello.getUri() != null && modello.getUri().startsWith("workspace:") ){
                 streamProvider = new CmisStreamProvider(cmisUrl, cmisUser, cmisPassword, modello.getUri());
             } else {
                 streamProvider = new FileStreamProvider(modello.getUri());
             }
             Template template = new Template(streamProvider, modello.getTitolo(), modello.getDescrizione(), null, ruleSet);
             ooopsPlugin.addTemplate(template);
         }
     }
 
     private static void forms(Database db) {
         
         Register.registerForm(db.getEntityManagerFactory(),
                               null,
                               Costante.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               null,
                               Etichetta.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                 "classpath:com/axiastudio/suite/base/forms/giunta.ui",
                 Giunta.class,
                 Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/base/forms/ufficio.ui",
                               Ufficio.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/base/forms/utente.ui",
                               Utente.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               null,
                               AlboProfessionale.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               null,
                               TitoloSoggetto.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               null,
                               Titolo.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               null,
                               Oggetto.class,
                               Window.class);
         
         Register.registerForm(db.getEntityManagerFactory(),
                               null,
                               Stato.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/anagrafiche/forms/gruppo.ui",
                               Gruppo.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/anagrafiche/forms/relazione.ui",
                               Relazione.class,
                               Window.class);
         
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/anagrafiche/forms/soggetto.ui",
                               Soggetto.class,
                               FormSoggetto.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/anagrafiche/forms/relazionesoggetto.ui",
                               RelazioneSoggetto.class,
                               FormRelazioneSoggetto.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/anagrafiche/forms/grupposoggetto.ui",
                               GruppoSoggetto.class,
                               Dialog.class);
         
         Register.registerUtility(FormQuickInsertSoggetto.class, IQuickInsertDialog.class, Soggetto.class.getName());
         
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/anagrafiche/forms/indirizzo.ui",
                               Indirizzo.class,
                               FormIndirizzo.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/anagrafiche/forms/riferimento.ui",
                               Riferimento.class,
                               Dialog.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/pratiche/forms/pratica.ui",
                               Pratica.class,
                               FormPratica.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/pratiche/forms/tipopratica.ui",
                               TipoPratica.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               null,
                               Dipendenza.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/protocollo/forms/motivazioneannullamento.ui",
                               MotivazioneAnnullamento.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/protocollo/forms/annullamentoprotocollo.ui",
                               AnnullamentoProtocollo.class,
                               FormAnnullamentoProtocollo.class);
         
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/pratiche/forms/dipendenzapratica.ui",
                               DipendenzaPratica.class,
                               FormDipendenzaPratica.class);
         
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/protocollo/forms/soggettoprotocollo.ui",
                               SoggettoProtocollo.class,
                               FormSoggettoProtocollo.class);
         
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/protocollo/forms/soggettoprotocollo.ui",
                               SoggettoRiservatoProtocollo.class,
                               FormSoggettoProtocollo.class);
         
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/protocollo/forms/praticaprotocollo.ui",
                               PraticaProtocollo.class,
                               FormPraticaProtocollo.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/protocollo/forms/protocollo.ui",
                               Protocollo.class,
                               FormProtocollo.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               null,
                               Fascicolo.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/pubblicazioni/forms/pubblicazione.ui",
                               Pubblicazione.class,
                               FormPubblicazione.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               null,
                               Carica.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               null,
                               Commissione.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/sedute/forms/caricacommissione.ui",
                               CaricaCommissione.class,
                               Window.class);
         
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/sedute/forms/tiposeduta.ui",
                               TipoSeduta.class,
                               FormTipoSeduta.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/sedute/forms/seduta.ui",
                               Seduta.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/finanziaria/forms/servizio.ui",
                               Servizio.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               null,
                               Capitolo.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/deliberedetermine/forms/determina.ui",
                               Determina.class,
                               FormDetermina.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/deliberedetermine/forms/movimentodetermina.ui",
                               MovimentoDetermina.class,
                               Dialog.class);
         
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/procedimenti/forms/norma.ui",
                               Norma.class,
                               Window.class);
        
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/procedimenti/forms/procedimento.ui",
                               Procedimento.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                 "classpath:com/axiastudio/suite/procedimenti/forms/faseprocedimento.ui",
                 FaseProcedimento.class,
                 FormFaseProcedimento.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/procedimenti/forms/delega.ui",
                               Delega.class,
                               FormDelega.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               null,
                               Fase.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/anagrafiche/forms/titolostudio.ui",
                               TitoloStudio.class,
                               Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                               "classpath:com/axiastudio/suite/anagrafiche/forms/titolostudiosoggetto.ui",
                               TitoloStudioSoggetto.class,
                               Dialog.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                 "classpath:com/axiastudio/suite/modelli/forms/modello.ui",
                 Modello.class,
                 Window.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                 "classpath:com/axiastudio/suite/modelli/forms/segnalibro.ui",
                 Segnalibro.class,
                 Dialog.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                 "classpath:com/axiastudio/suite/richieste/forms/richiesta.ui",
                 Richiesta.class,
                 FormRichiesta.class);
 
         Register.registerForm(db.getEntityManagerFactory(),
                 null,
                 DestinatarioUfficio.class,
                 Window.class);
 
     }
     
 }
