 package structure;
 
 import java.util.*;
 
 /**
  * @author mugnier
  *
  */
 public class BaseFaits
 {
 	private AtomSet listeAtomes;//l'ensemble des faits (atomes)
 	private ArrayList<Term> ensembleTermes;//l'ensemble des termes prsents
 	
 	
 	//************************************************************************************
 	// CONSTRUCTORS
   //************************************************************************************
 	
 	/**
 	 * Constructeur de la classe 'BaseFaits'
 	 * cre une base de faits vide
 	 */
 	public BaseFaits()
 	{
 		listeAtomes = new AtomSet();
 		ensembleTermes = new ArrayList<Term>();
 	}
 	
 	/**
 	 * Constructeur de la classe 'BaseFaits'
 	 * @param baseFaits les faits, passs sous la forme "atom1;...;atomk"
 	 */
 	public BaseFaits(String baseFaits)
 	{
 		listeAtomes = new AtomSet();
 		ensembleTermes = new ArrayList<Term>();
 		creerBaseFaits(baseFaits);	
 	}
 	
 	//************************************************************************************
   // METHODS
   //************************************************************************************
 	
 	/**
    * Ajoute des faits  la base de faits s'ils n'apparaissaient pas dj
    * @param faits les faits  ajouter (seuls ceux n'apparaissant pas dans la base seront ajouts)
    */
   public void ajouterNouveauxFaits(ArrayList<Atom> faits)
   // Spec Interne : la liste des termes apparaissant dans la base est galement
   // mise  jour
   {
     for(int i=0;i<faits.size();i++)
       ajouterNouveauFait(faits.get(i));
   }
   
   /**
    * Ajoute un fait  la base de faits s'il n'apparait pas dj
    * @param fait le fait  ajouter (ne sera ajout que s'il n'apparait pas dj)
    */
   public void ajouterNouveauFait(Atom fait)
   // Spec Interne : la liste des termes apparaissant dans la base est galement
   // mise  jour
   {
     System.out.println("Ajouter nouveau faits");
       if(!atomeExiste(fait))
       {
         System.out.println("not exist");
 
         listeAtomes.add(fait);
         for(int j=0;j<fait.getListeTermes().size();j++)
         {
           Term t = new Term(fait.getListeTermes().get(j).getLabel(),fait.getListeTermes().get(j).isConstant());//On cre un nouveau terme
           t = ajouterTerme(t); // ceci ajoute le terme dans la liste des termes de la base de faits s'il n'existait pas dj
           listeAtomes.get(listeAtomes.size()-1).getListeTermes().set(j,t);
         }
       }
   }
 
   /**
    * Ajoute un terme  la liste des termes s'il n'existe pas dj.
    * @param t le terme  potentiellement ajouter
    * @return un sommet terme, soit t s'il a t insr, soit le sommet terme qui existait dj dans la liste des sommets termes
    */
   public Term ajouterTerme(Term t)
   {
     for(Term terme : ensembleTermes)
     {
       if(terme.equals(t))
         return terme;
     }
    
     ensembleTermes.add(t);
     return t;
   } 
   
   
   public boolean contains(Atom a)
   {
     return listeAtomes.contains(a);
   }
 	
   
 	//************************************************************************
   // GETTERS / SETTERS
   // ************************************************************************
 
 	public AtomSet getListeAtomes() {
 		return listeAtomes;
 	}
 
 	public void setListeAtomes(AtomSet listeAtomes) {
 		this.listeAtomes = listeAtomes;
 	}
 
 	public ArrayList<Term> getEnsembleTermes() {
 		return (ArrayList<Term>)ensembleTermes.clone();
 	}
   
   public void addAll(ArrayList<Atom> list)
   {
     for(Atom a: list)
       this.ajouterNouveauFait(a);
   }
 
 	// ************************************************************************
   // OVERRIDE OBJECT
   // ************************************************************************
   
   /**
    * retourne une description de la base de faits
    * @return description de la base de faits
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
     String s = "nombre de faits : "+listeAtomes.size()+ "\n";
     
     s+="liste des faits : ";   
     for(Atom a : listeAtomes)
       s+=a+" ; ";
     
     s+="\nliste des termes : ";
     for(Term t : ensembleTermes)
       s+=t+" ; ";
     
     return s;
   }
 	
 	//************************************************************************
   // PRIVATES METHODS
   // ************************************************************************
 
 	private void creerBaseFaits(String baseFaits)
 	//Prrequis : le format de la base de faits est suppos correct
    	{
 		Term t;
    		StringTokenizer st = new StringTokenizer(baseFaits,";");
    		while(st.hasMoreTokens())
    		{
    			String s = st.nextToken(); // s reprsente un atome
    			Atom a = new Atom(s);
    			ajouterAtome(a);//On ajoute un atome  la liste des atomes
    			ArrayList<Term> termes = a.getListeTermes();
    			for (int i = 0; i < termes.size(); i ++)
    			{
    				t = ajouterTerme(termes.get(i));
    				a.getListeTermes().set(i,t);
    			}
    		}
    	}
 	
 	
 	/**
 	 * Ajoute un atome  la base de faits sans autre vrification
 	 * tous les termes de l'atome doivent dj exister dans la base
 	 * @param a l'atome  ajouter
 	 * @return la position o l'atome 'a' a t ajout (s'il existait dj il est ajout en double)
 	 */
 	private int ajouterAtome(Atom a)
 	{
 		listeAtomes.add(a);
 		return listeAtomes.size()-1;
 	}
 	
 	/**
 	 * Teste l'existence d'un atome dans la base
 	 * @param a l'atome dont on teste l'existence
 	 * @return vrai si l'atome existe dj, faux sinon
 	 */
 	public boolean atomeExiste(Atom a)
 	{
 	  return listeAtomes.contains(a);
 	}
 	
 	
 	
 }
