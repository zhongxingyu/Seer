 package solitaire.controleur;
 
 import java.awt.Component;
 
 import javax.swing.JFrame;
 
import solitaire.application.Carte;
 import solitaire.application.TasDeCartesColorees;
 import solitaire.application.Usine;
 import solitaire.presentation.*;
 import solitaire.usine.CUsine;
 
 public class CTasDeCarteColorees extends TasDeCartesColorees
 {
     private PTasDeCarteColoree p;
 
     private CTasDeCarte tas;
 
     public CTasDeCarteColorees( String nom, int couleur, Usine usine )
     {
         super( nom, couleur, usine );
 
         p = new PTasDeCarteColoree( this );
     }
 
    public void empiler( Carte c )
     {
         System.out.println( "empiler CTasDeCarteColoree" );
         if ( isEmpilable( c ) )
         {
             super.empiler( c );
            p.empiler( ( (CCarte) c ).getPresentation() );
         }
         else
             System.out.println("impossible d'empiler sur le tas de carte coloree");
     }
     
     public PTasDeCarteColoree getPresentation()
     {
         return p;
     }
 
     public void setP( PTasDeCarteColoree p )
     {
         this.p = p;
     }
 
    public CTasDeCarte getTas()
    {
        return tas;
    }

    public void setTas( CTasDeCarte tas )
    {
        this.tas = tas;
    }
    
     public static void main( String[] args )
     {
         JFrame frame = new JFrame( "Test CTasDeCarteColoree" );
 
         CCarte c1 = new CCarte( 1, 2 );
         CCarte c2 = new CCarte( 2, 2 );
         CCarte c3 = new CCarte( 13, 3 );
         CCarte c4 = new CCarte( 12, 2 );
         c1.setFaceVisible( true );
         c2.setFaceVisible( true );
         c3.setFaceVisible( true );
         c4.setFaceVisible( true );
 
         CUsine factory = new CUsine();
         CTasDeCarteColorees tasColoree = new CTasDeCarteColorees( "pique", 2, factory );
 
         try
         {
             tasColoree.empiler( c1 );
             tasColoree.empiler( c2 );
         }
         catch ( Exception e )
         {
             e.printStackTrace();
         }
         frame.getContentPane().add( tasColoree.getPresentation() );
 
         frame.setVisible( true );
         frame.setSize( 200, 400 );
         frame.setLocationRelativeTo( frame.getParent() );
         frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
     }
 
   
 
 }
