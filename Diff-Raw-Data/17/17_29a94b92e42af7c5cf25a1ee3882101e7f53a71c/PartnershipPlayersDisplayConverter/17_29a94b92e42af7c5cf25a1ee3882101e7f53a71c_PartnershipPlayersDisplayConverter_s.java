 package com.jwxicc.cricket.jsf.convert;
 
 import java.util.List;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.FacesConverter;
 
 import com.jwxicc.cricket.entity.PartnershipPlayer;
 
 @FacesConverter(value = "partnershipPlayersConverter")
 public class PartnershipPlayersDisplayConverter implements Converter {
 
 	@Override
 	public Object getAsObject(FacesContext context, UIComponent component, String value) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getAsString(FacesContext context, UIComponent component, Object value) {
 		String display = "";
 		List<PartnershipPlayer> players = (List<PartnershipPlayer>) value;
 		// use booleans to determine what to add
 		boolean addContribution = false;
 
 		// get the players
 		PartnershipPlayer firstPlayer = players.get(0);
 		PartnershipPlayer secondPlayer = players.get(1);
 
 		// if contribution has been set, display it
 		if (firstPlayer.getContribution() != 0 || secondPlayer.getContribution() != 0) {
 			addContribution = true;
 		}
 
 		// add first player to display
 		display += getPlayerDisplay(firstPlayer, addContribution);
 		display += "/";
 
 		// add second player to display
 		display += getPlayerDisplay(secondPlayer, addContribution);
 
 		return display;
 	}
 
 	private String getPlayerDisplay(PartnershipPlayer player, boolean addContribution) {
 		String display = "";
 		display += player.getPlayer().getScorecardName();
 		if (!player.isOutStatus())
 			display += "*";
 		if (addContribution)
 			display += " (" + player.getContribution() + ")";
 
 		return display;
 	}
 
 }
