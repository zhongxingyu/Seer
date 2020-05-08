 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author jonathan
  */
 public class GPS extends Mod {
 	/**
 	 *
 	 * NOTE: The player's coordinate map like this:
 	 * X-value: Latitude (north-south)
 	 * Y-value: Altitude
 	 * Z-value: Longitude (east-west)
 	 *
 	 * @param player
 	 * @param command
 	 * @param isAdmin
 	 * @return boolean
 	 */
 	protected boolean parseCommand(Player player, String[] tokens) {
 		String command = tokens[0].substring(1);
 		
 		if(command.equalsIgnoreCase("gps")) {
 			Player target;
 			if( tokens.length > 1 ) {
 				target = Server.getPlayer(tokens[1]);
 			} else {
 				target = player;
 			}
 
 			this.getCoordinates(player, target);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return "!gps";
 	}
 
 	@Override
 	public boolean onPlayerCommand(Player player, String[] command) {
 		return this.parseCommand(player, command);
 	}
 
 	@Override
 	public boolean onPlayerChat(Player player, String command) {
 		String[] tokens = command.split(" ");
 		return this.parseCommand(player, tokens);
 	}
 
 	protected String generateOrientation(Float degrees) {
 		degrees = (degrees+270)%360;
 		String textDirection = "";
 
 		// Determine North/South facing.
 		if( degrees <= 78.75 || degrees > 281.25 ) {
 			textDirection = "N";
 		} else if( degrees > 101.25 && degrees <= 258.75 ) {
 			textDirection = "S";
 		}
 
 		// Determine East/West facing.
 		if (degrees > 11.25 && degrees <= 168.75 ) {
 			textDirection += "E";
 		} else if( degrees > 190.25 && degrees <= 348.75 ) {
 			textDirection += "W";
 		}
 
 		// Determine Additional north/east/south/west facing for 3-letter precision.
 		if( (degrees > 326.25 || degrees <= 33.75) && !textDirection.equalsIgnoreCase("N") ) {
 			textDirection = "N"+textDirection;
 		} else if( (degrees > 56.25 && degrees <= 123.75) && !textDirection.equalsIgnoreCase("E") ) {
 			textDirection = "E"+textDirection;
 		} else if( (degrees > 146.25 && degrees <= 213.75) && !textDirection.equalsIgnoreCase("S") ) {
 			textDirection = "S"+textDirection;
 		} else if( (degrees > 236.25 && degrees <= 303.75) && !textDirection.equalsIgnoreCase("W") ) {
 			textDirection = "W"+textDirection;
 		}
 		
 
 		String directionPadding = "";
 		if( textDirection.length() < 2 ) directionPadding += '_';
 		if( textDirection.length() < 3 ) directionPadding += '_';
 		
		return String.format("(%s\u00A7d%s\u00A77) %s\u00A7d%.1f\u00A7f",
 			directionPadding, textDirection,
 			this.getDegreesPadding((double)degrees), degrees
 		);
 	}
 
 	protected String generateLatitude(Double degrees) {
 		return String.format("\u00A77%s\u00A7a%.1f %s\u00A7f",
 			this.getDegreesPadding(Math.abs(degrees)), Math.abs(degrees),
 			(degrees<0?"N":degrees>0?"S":"")
 		);
 	}
 	protected String generateLongitude(Double degrees) {
 		return String.format("\u00A77%s\u00A7a%.1f %s\u00A7f",
 			this.getDegreesPadding(Math.abs(degrees)), Math.abs(degrees),
 			(degrees<0?"E":degrees>0?"W":"")
 		);
 	}
 	protected String generateAltitude(Double altitude) {
 		return Color.LightGray + this.getDegreesPadding(altitude) +
 						Color.DarkPurple + String.format("%.0f", altitude) +
 						Color.LightGray + "m" + Color.White;
 	}
 
 	protected String getDegreesPadding(Double degrees) {
 		String padding = "";
 		if( degrees < 100 ) padding += "0";
 		if( degrees < 10 ) padding += "0";
 		return padding;
 	}
 	
 	protected void getCoordinates(Player player, Player target) {
 		Location loc = target.getLocation();
 		Double altitude = loc.getY();
 
 		String playerLabel = "";
 		if( player.getUniqueId() != target.getUniqueId() ) {
 			playerLabel = String.format("\u00A77%s:\u00A7f ", target.getName());
 		}
 
 		player.sendChat(String.format(
 			"%s\u00A77Loc:[ %s\u00A77, %s\u00A77 ] Facing:[ %s\u00A77 ] Alt:[ %s\u00A77 ]",
 			playerLabel,
 			this.generateLatitude(loc.getX()),
 			this.generateLongitude(loc.getZ()),
 			this.generateOrientation(target.getRotation()),
 			this.generateAltitude(loc.getY())
 		));
 	}
 }
