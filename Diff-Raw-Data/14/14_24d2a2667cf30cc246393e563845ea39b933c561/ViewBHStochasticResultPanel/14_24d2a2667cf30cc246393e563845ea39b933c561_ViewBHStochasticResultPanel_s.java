 package org.bh.plugin.stochasticResultAnalysis;
 
 import org.bh.data.DTOScenario;
 import org.bh.data.types.DistributionMap;
 import org.bh.gui.View;
 import org.bh.gui.ViewException;
 
 public class ViewBHStochasticResultPanel extends View{
     public ViewBHStochasticResultPanel(DTOScenario scenario, DistributionMap result) throws ViewException{
        super(new BHStochasticResultPanel(scenario, result));
     }
 }
