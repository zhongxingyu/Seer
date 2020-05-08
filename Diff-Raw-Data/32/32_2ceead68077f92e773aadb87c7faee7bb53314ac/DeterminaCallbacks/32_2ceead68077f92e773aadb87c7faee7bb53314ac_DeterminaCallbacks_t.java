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
 
 package com.axiastudio.suite.deliberedetermine;
 
 import com.axiastudio.pypapi.Register;
 import com.axiastudio.pypapi.annotations.Callback;
 import com.axiastudio.pypapi.annotations.CallbackType;
 import com.axiastudio.pypapi.db.Validation;
 import com.axiastudio.suite.base.entities.IUtente;
 import com.axiastudio.suite.base.entities.UfficioUtente;
 import com.axiastudio.suite.base.entities.Utente;
 import com.axiastudio.suite.deliberedetermine.entities.Determina;
 import com.axiastudio.suite.procedimenti.GestoreDeleghe;
 import com.axiastudio.suite.procedimenti.IGestoreDeleghe;
 import com.axiastudio.suite.procedimenti.entities.CodiceCarica;
 import com.axiastudio.suite.deliberedetermine.entities.ServizioDetermina;
 
 /**
  * User: tiziano
  * Date: 14/01/14
  * Time: 10:36
  */
 public class DeterminaCallbacks {
 
     @Callback(type= CallbackType.BEFORECOMMIT)
     public static Validation validaDetermina(Determina determina){
         String msg = "";
         Boolean res = true;
         Boolean inUfficioGestore = false;
         Utente autenticato = (Utente) Register.queryUtility(IUtente.class);
         // se l'utente non è istruttore non può inserire o modificare pratiche,
         if( !autenticato.getIstruttorepratiche() ){
             msg = "Devi avere come ruolo \"istruttore\" per poter inserire\n";
             msg += "o modificare una determina.";
             return new Validation(false, msg);
         }
         for( UfficioUtente uu: autenticato.getUfficioUtenteCollection() ){
             if( uu.getUfficio().equals(determina.getPratica().getGestione()) && uu.getModificapratica() ){
                 // se la pratica è riservata, mi serve anche il flag
                 if( !determina.getPratica().getRiservata() || uu.getRiservato() ){
                     inUfficioGestore = true;
                     break;
                 }
             }
         }
         // se l'utente non è inserito nell'ufficio gestore con flag modificapratiche non può modificare
         GestoreDeleghe gestoreDeleghe = (GestoreDeleghe) Register.queryUtility(IGestoreDeleghe.class);
         if( !inUfficioGestore && !gestoreDeleghe.checkTitoloODelega(CodiceCarica.RESPONSABILE_DI_BILANCIO) ){
             msg = "Per modificare la determina devi appartenere all'ufficio gestore della pratica con i permessi di modifica, o essere responsabile di bilancio.";
             return new Validation(false, msg);
         }
        if( determina.getId() != null ){
            /* almeno e massimo un servizio principale */
            if( determina.getServizioDeterminaCollection() == null || determina.getServizioDeterminaCollection().isEmpty() ){
                msg += "Deve essere dichiarato almeno servizio.";
                return new Validation(false, msg);
            }
            ServizioDetermina servizioPrincipale = null;
            int nrServiziPrincipali = 0;
            for( ServizioDetermina servizio: determina.getServizioDeterminaCollection() ){
                if( servizio.getPrincipale() ){
                    nrServiziPrincipali += 1;
                }
            }
            if( nrServiziPrincipali != 1 ){
                msg += "E' possibile e necessario impostare un solo servizio principale.\n";
                return new Validation(false, msg);
             }
         }
 
         return new Validation(true);
     }
 }
