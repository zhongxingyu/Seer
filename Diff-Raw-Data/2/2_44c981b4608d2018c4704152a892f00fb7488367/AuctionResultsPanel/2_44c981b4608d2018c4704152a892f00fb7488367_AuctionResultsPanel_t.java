 package edu.umich.eecs.tac.logviewer.gui;
 
 import edu.umich.eecs.tac.props.Query;
 import edu.umich.eecs.tac.props.QueryReport;
 import edu.umich.eecs.tac.props.Ad;
 import edu.umich.eecs.tac.logviewer.info.GameInfo;
 import edu.umich.eecs.tac.logviewer.info.Advertiser;
 import static edu.umich.eecs.tac.logviewer.util.VisualizerUtils.*;
 
 import javax.swing.*;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 import java.awt.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: leecallender
  * Date: Feb 17, 2009
  * Time: 11:28:45 AM
  * To change this template use File | Settings | File Templates.
  *
  * This panel displays the average positions of all advertisers
  * for a given auction over the course of a day.
  */
 //TODO-This should probably be tested
 public class AuctionResultsPanel {
   public final static String NA = "";
 
   JPanel mainPane;
   JLabel[] positionLabels;
   int[] indexes;
   Advertiser[] advertisers;
 
   Query query;
   PositiveBoundedRangeModel dayModel;
   GameInfo gameInfo;
 
   public AuctionResultsPanel(Query query, GameInfo gameInfo, PositiveBoundedRangeModel dm){
     this.query = query;
     this.gameInfo = gameInfo;
     this.dayModel = dm;
     this.advertisers = gameInfo.getAdvertisers();
 
     if(dayModel != null) {
 	    dayModel.addChangeListener(new ChangeListener() {
 		    public void stateChanged(ChangeEvent ce) {
 			    updateMePlz();
 		    }
 		  });
     }
 
     mainPane = new JPanel();
     mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
     mainPane.setBorder(BorderFactory.createTitledBorder
 			   (BorderFactory.createEtchedBorder(),formatToString(query)));
     mainPane.setMinimumSize(new Dimension(105, 155));
     mainPane.setPreferredSize(new Dimension(105, 155));
     mainPane.setBackground(Color.WHITE);
 
     indexes = new int[advertisers.length];
     positionLabels = new JLabel[advertisers.length];
     for(int i = 0; i < indexes.length; i++){
       indexes[i] = i;
       positionLabels[indexes.length-i-1] = new JLabel(NA);
       positionLabels[indexes.length-i-1].setForeground(advertisers[i].getColor());
       mainPane.add(positionLabels[indexes.length-i-1]);
     }
     
     updateMePlz();
   }
 
   private void updateMePlz(){
     int day = dayModel.getCurrent();
     double[] averagePosition = new double[advertisers.length];
     QueryReport report = advertisers[0].getQueryReport(day+1); 
     if(report == null){
       noQueryReportDay();
     }else{
       for(int i = 0; i < indexes.length; i++){
         averagePosition[indexes[i]] = report.getPosition(query, advertisers[indexes[i]].getName());
        //System.out.println(averagePosition[indexes[i]]+","+advertisers[indexes[i]].getName());
       }
       hardSort(averagePosition, indexes);
 
       for(int i = 0; i < indexes.length; i++){
         Ad ad = report.getAd(query, advertisers[indexes[i]].getName());
         String adString;
         if(ad == null)
           adString = NA;
         else
           adString = formatToString(ad);
 
         positionLabels[i].setText(adString);
         positionLabels[i].setForeground(advertisers[indexes[i]].getColor());
       }
     }
   }
 
   /**
    * Display 'N/A' for everyone
    */
   private void noQueryReportDay(){
     for(int i = 0; i < indexes.length; i++){
       indexes[i] = i;
       positionLabels[i].setText(NA);
       positionLabels[i].setForeground(advertisers[i].getColor());
     }
   }
 
   public Component getMainPane() {
     return mainPane;
   }
 
   
 }
