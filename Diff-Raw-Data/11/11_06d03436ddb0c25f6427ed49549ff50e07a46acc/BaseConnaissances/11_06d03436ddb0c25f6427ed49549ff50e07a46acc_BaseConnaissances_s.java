 /**
  * Copyright (C) 2011 Thibaut Marmin
  * Copyright (c) 2011 Clment Sipieter
  * 
  * This file is part of N2P.
  *
  * N2P is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License.
  *
  * N2P is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with N2P. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package structure;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 public class BaseConnaissances
 {
   private BaseFaits bf;// base de faits
   private BaseRegles br;// base de rgles
   private boolean is_saturated;
 
   // ************************************************************************************
   // CONSTRUCTORS
   // ************************************************************************************
 
   /**
    * Cration d'une base de connaissance vide
    */
   public BaseConnaissances()
   {
     this.init(new BaseFaits(), new BaseRegles());
   }
 
   /**
    * Cration d'une base de connaissance initialise avec la BF et la BR passes
    * en paramtre
    * 
    * @param BF
    * @param BR
    */
   public BaseConnaissances(BaseFaits BF, BaseRegles BR)
   {
     this.init(BF, BR);
   }
 
   // ************************************************************************************
   // METHODS
   // ************************************************************************************
 
   /**
    * Chargement d'une base de connaissance depuis un fichier 
    * @param path
    *          le chemin vers le fichier  charger
    * @return une instance de BaseConnaissances initialiser avec les faits et
    *         regles du fichier
    */
   public Boolean load(String path)
   {
     BufferedReader reader = null;
     String line;
     int line_nbr = 0;
 
     this.is_saturated = false;
 
     try
     {
       reader = new BufferedReader(new FileReader(path));
       while ((line = reader.readLine()) != null)
       {
         ++line_nbr;
         if (RuleBasedSystemLib.isFact(line))
         {
           bf.ajouterNouveauFait(new Atom(line));
         } 
         else if (RuleBasedSystemLib.isRule(line))
         {
           br.add(new Rule(line));
         }
         else if(RuleBasedSystemLib.isBlank(line))
         {}
         else
         {
           System.out.println("Error line "+line_nbr+" : "+line);
         }
       }
     } catch (IOException e)
     {
       System.out.println("Erreur lors de la lecture du fichier");
       return false;
     }
 
     return true;
   }
 
   /**
    * Saturation de la base de faits
    */
   public void saturation()
   {
     AtomSet new_faits = new AtomSet();
     Atom a;
     Rule r;
     
     while (true)
     {
       new_faits.clear();
       for (Rule rule : this.br)
       {
         r = rule.clone();
         for (Substitution h : RuleBasedSystemLib.homomorphisme(
             r.getHypothese(), this.bf.getListeAtomes()))
         {
          a = r.getConclusion();
           a.substitue(h);
           if (!new_faits.contains(a) && !this.bf.contains(a))
          {
            System.out.println("Add "+a);
             new_faits.add(a.clone());
            System.out.println(new_faits);
          }
         }
       }
       if (new_faits.size() == 0)     
         break;     
       else
         this.bf.addAll(new_faits);
 
     }
 
   }
 
   /**
    * @param a
    * @return true si la base de faits contiens un atome gal a "a", false sinon.
    */
   public boolean valueOf(Atom a)
   {
     if (!this.is_saturated)
       this.saturation();
 
     return bf.contains(a);
   }
   
   /**
    * Retourne l'ensemble des substitution rendant vrai r
    * @param r
    * @return
    */
   public LinkedList<Substitution> instanceOf(AtomSet atomset)
   {
     if (!this.is_saturated)
       this.saturation();
    
     SubstitutionsList subList = new SubstitutionsList();
     return RuleBasedSystemLib.homomorphisme(atomset, bf.getListeAtomes());
   }
 
   /**
    * Retourne l'ensemble des substitution rendant vrai r
    * @param r
    * @return
    */
   public SubstitutionsList instanceO0f(Rule r)
   {
     if (!this.is_saturated)
       this.saturation();
 
     // initialisation avec les substitutions possible sur la "conclusion"
     SubstitutionsList subList = new SubstitutionsList();
     SubstitutionsList sublisttmp;
     Substitution s;
 
     Atom at = r.getConclusion();
     for (Atom fa : bf.getListeAtomes())
     {
       boolean err = false;
       if (at.predicatEqualsPredicatOf(fa))
       {
         s = new Substitution();
         for (int i = 0; i < fa.getListeTermes().size(); ++i)
         {
           if (!s.ajoutSiCompatible(new CoupleTermes(at.getListeTermes().get(i),
               fa.getListeTermes().get(i))))
             err = true;
         }
         if (!err)
           subList.add(s);
       }
     }
 
     for (Atom ra : r.getHypothese()) // A(x,y), B(y,z)
     {
       sublisttmp = new SubstitutionsList();
       for (Atom fa : bf.getListeAtomes())
       {
         boolean err = false;
         if (ra.predicatEqualsPredicatOf(fa))
         {
           s = new Substitution();
           for (int i = 0; i < fa.getListeTermes().size(); ++i)
           {
             if (!s.ajoutSiCompatible(new CoupleTermes(ra.getListeTermes()
                 .get(i), fa.getListeTermes().get(i))))
               err = true;
           }
           if (!err)
             sublisttmp.add(s);
         }
       }
 
       subList = subList.intersectionWidth(sublisttmp);
     }
 
     return subList;
   }
 
   /**
    * Rinitialisation de la base de connaissance
    */
   public void clear()
   {
     this.init(new BaseFaits(), new BaseRegles());
   }
   
   // ******************************************************************************
   // GETTERS / SETTERS
   // ******************************************************************************
   
   public BaseFaits getBaseFaits()
   {
     return bf;
   }
 
   public BaseRegles getBaseRegles()
   {
     return br;
   }
 
   public void ajouterRegle(Rule r)
   {
     this.br.add(r);
   }
 
   public void ajouterRegle(ArrayList<Rule> list)
   {
     this.br.addAll(list);
   }
 
   public void ajouterFait(Atom a)
   {
     bf.ajouterNouveauFait(a);
   }
 
   public void ajouterFait(ArrayList<Atom> faits)
   {
     bf.ajouterNouveauxFaits(faits);
   }
 
   // ************************************************************************************
   // OVERRIDE METHODS
   // ************************************************************************************
   @Override
   public String toString()
   {
     return "\n******Base de faits******\n" + bf + "\n"
         + "\n******Base de rgles*****\n" + br;
   }
 
   // ************************************************************************************
   // PRIVATE METHODS
   // ************************************************************************************
   private void init(BaseFaits BF, BaseRegles BR)
   {
     this.bf = BF;
     this.br = BR;
     this.is_saturated = false;
   }
 
 }
