 /*
  * Geotools - OpenSource mapping toolkit
  * (C) 2002, Center for Computational Geography
  * (C) 2000, Institut de Recherche pour le Dveloppement
  * (C) 1999, Pches et Ocans Canada
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation; either
  *    version 2.1 of the License, or (at your option) any later version.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  *
  *    You should have received a copy of the GNU Lesser General Public
  *    License along with this library; if not, write to the Free Software
  *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *
  *                   THIS IS A TEMPORARY CLASS
  *
  *    This is a placeholder for future <code>Unit</code> class.
  *    This skeleton will be removed when the real classes from
  *    JSR-108: Units specification will be publicly available.
  */
 package org.geotools.resources.units;
 
 // Ressources
 import java.util.MissingResourceException;
 
 
 /**
  * Liste de noms d'units qui dpendront de la langue de l'utilisateur. L'usager
  * ne devrait pas crer lui-mme des instances de cette classe. Une instance
  * statique sera cre une fois pour toute lors du chargement de cette classe,
  * et les divers resources seront mises  la disposition du dveloppeur
  * via les mthodes statiques.
  *
  * @version 1.0
  * @author Martin Desruisseaux
  */
 public class Units extends SymbolResources {
     /**
      * Instance statique cre une fois pour toute.
      * Tous les messages seront construits  partir
      * de cette instance.
      */
     private final static Units resources =
        (Units) getBundle("org.geotools.resources.units.Units");
 
     /**
      * Initialise les ressources par dfaut. Ces ressources ne seront pas forcment dans
      * la langue de l'utilisateur. Il s'agit plutt de ressources  utiliser par dfaut
      * si aucune n'est disponible dans la langue de l'utilisateur. Ce constructeur est
      * rserv  un usage interne et ne devrait pas tre appell directement.
      */
     public Units() {
         super(true ? Units_en.contents : Units_fr.contents);
     }
 
     /**
      * Initialise les ressources en
      * utilisant la liste spcifie.
      */
     Units(final Object[] contents) {
         super(contents);
     }
 
     /**
      * Retourne la valeur associe  la cle spcifie, ou <code>key</code> s'il
      * n'y en a pas. A la diffrence de <code>format(String)</code>, cette mthode
      * ne lance pas d'exception si la resource n'est pas trouve.
      */
     public static String localize(final String key) {
         if (key==null) {
             return key;
         }
         final Object res=resources.handleGetObject(key);
         return (res instanceof String) ? (String) res : key;
     }
 }
