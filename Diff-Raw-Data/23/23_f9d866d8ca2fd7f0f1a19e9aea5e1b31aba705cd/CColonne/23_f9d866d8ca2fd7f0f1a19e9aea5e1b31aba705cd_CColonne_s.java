 package solitaire.controleur;
 
 import javax.swing.JFrame;
 import javax.swing.RepaintManager;
 
 import solitaire.application.Carte;
 import solitaire.application.Colonne;
 import solitaire.application.Usine;
 import solitaire.presentation.PColonne;
 import solitaire.usine.CUsine;
 
 public class CColonne extends Colonne
 {
 
     private static int YOffsetAlt = 30;
 
     private static int YOffsetCache = 10;
 
     private static int XOffset = 0;
 
     private CTasDeCarte tasCachee;
 
     private CTasDeCarteAlterne tasVisible;
 
     private PColonne p;
 
     public CColonne( String nom, Usine usine )
     {
         super( nom, usine );
         this.tasCachee = (CTasDeCarte) cachees;
         this.tasVisible = (CTasDeCarteAlterne) visibles;
         p = new PColonne( this );
         p.getTasCachee().setXoffset( XOffset );
         p.getTasCachee().setYoffset( YOffsetCache );
         p.getTasVisible().setXoffset( XOffset );
         p.getTasVisible().setYoffset( YOffsetAlt );
     }
 
     @Override
     public void retournerCarte() throws Exception
     {
         System.out.println( "retournerCarte de CColonne" );
         if ( isCarteRetournable() )
             super.retournerCarte();
     }
 
     @Override
     public void empiler( Carte c )
     {
         if ( isEmpilable( c ) )
             super.empiler( c );
         else
             System.out.println( "carte non empilable sur ce tas de carte alternee" );
     }
 
     public void p2c_debutDnDDrag( CCarte carte )
     {
         CTasDeCarte tmp = new CTasDeCarte( "drag", new CUsine() ); // Tas
                                                                    // temporaire
                                                                    // dans le
                                                                    // mauvais
                                                                    // sens
         CTasDeCarte tmp2 = new CTasDeCarte( "drag", new CUsine() ); // Tas ds le
                                                                     // bon sens
 
         tmp2.getPresentation().setYoffset( 20 );
         if ( carte != null )
         {
             try
             {
                 while ( visibles.getSommet() != carte )
                 {
                     System.out.println( "on y ai" );
                     Carte tmpCarte = visibles.getSommet();
                     visibles.depiler();
                     tmp.empiler( tmpCarte );
                 }
                 visibles.depiler();
                 tmp.empiler( carte );
 
                 // le tas de carte intermediaire est dans le mauvais sens donc
                 // on le retourne
                 while(!tmp.isVide())
                 {
                     Carte c = tmp.getSommet();
                     tmp.depiler();
                     tmp2.empiler( c );
                 }                
                 
                 p.c2p_debutDnDValide( tmp2.getPresentation() );
             }
             catch ( Exception e )
             {
                 System.out.println( "empty" );
             }
         }
         else
         {
             p.c2p_debutDnDInvalide();
         }
     }
 
     public void p2c_finDnDDrag( CTasDeCarte tasTemp, boolean dropSuccess )
     {
         if(!dropSuccess)
        {
            CTasDeCarte tasAlEndroi=new CTasDeCarte( "temp", new CUsine() );
            while(!tasTemp.isVide())
            {
                Carte c = null;
                try
                {
                    c = tasTemp.getSommet();
                    tasTemp.depiler();
                    tasAlEndroi.empiler( c );                    
                }
                catch ( Exception e )
                {
                    System.out.println("erreur pour rempiler les cartes en cours de dplacement");
                    return;
                }                
            }
            visibles.empiler( tasAlEndroi );
        }
            
     }
 
     public void p2c_DragEnter( CTasDeCarte tas )
     {
         if ( isEmpilable( tas ) )
         {
             tas.getPresentation().c2p_isEmpilable();
         }
         else
             tas.getPresentation().c2p_isNotEmpilable();
     }
 
     public void finDnDDrop( CTasDeCarte tas )
     {
         if ( isEmpilable( tas ) )
         {
             empiler( tas );
             p.finDnDValide();
         }
         else
         {
             p.finDnDInvalid();
         }
 
     }
 
     public CTasDeCarteAlterne getTasVisible()
     {
         return tasVisible;
     }
 
     public void setTasVisible( CTasDeCarteAlterne tasVisible )
     {
         this.tasVisible = tasVisible;
     }
 
     public CTasDeCarte getTasCachee()
     {
         return tasCachee;
     }
 
     public void setTasCachee( CTasDeCarte tasCachee )
     {
         this.tasCachee = tasCachee;
     }
 
     public PColonne getPresentation()
     {
         return p;
     }
 
     public static void main( String[] args )
     {
         JFrame frame = new JFrame( "Test CColonne" );
         CUsine usine = new CUsine();
         CCarte c1 = new CCarte( 13, 2 );
         CCarte c2 = new CCarte( 12, 1 );
         CCarte c3 = new CCarte( 11, 2 );
         CCarte c4 = new CCarte( 10, 1 );
 
         CColonne c = new CColonne( "colonne", usine );
         CTasDeCarte tas = new CTasDeCarte( "tas", usine );
         tas.empiler( c1 );
         tas.empiler( c2 );
         tas.empiler( c3 );
         tas.empiler( c4 );
 
         c.setReserve( tas );
 
         try
         {
             c.retournerCarte();
 
         }
         catch ( Exception e )
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         frame.getContentPane().add( c.getPresentation() );
 
         frame.setVisible( true );
         frame.setSize( 100, 400 );
         frame.setLocationRelativeTo( frame.getParent() );
         frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
     }
 
 }
