 package dk.dma.aisservices.core.services.ais;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import dk.dma.aisservices.core.domain.AisClassAPosition;
 import dk.dma.aisservices.core.domain.AisClassAStatic;
 import dk.dma.aisservices.core.domain.AisVesselPosition;
 import dk.dma.aisservices.core.domain.AisVesselStatic;
 import dk.dma.aisservices.core.domain.AisVesselTarget;
 import dk.frv.ais.country.CountryMapper;
 import dk.frv.ais.country.MidCountry;
 import dk.frv.ais.message.NavigationalStatus;
 
 public class DetailedAisTarget {
 	
 	protected long id;
 	protected long mmsi;
 	protected String vesselClass;
 	protected String lastReceived;
 	protected long currentTime;
 	protected String lat;
 	protected String lon;
 	protected String cog;
 	protected boolean moored;
 	protected String vesselType;
 	protected Short length = null;
 	protected Byte width = null;
 	protected String sog;
 	protected String name;
 	protected String callsign;
 	protected String imoNo = "N/A";
 	protected String cargo = "N/A";
 	protected String country;
 	protected String draught = "N/A";
 	protected String heading = "N/A";
 	protected String rot = "N/A";
 	protected String destination = "N/A";
 	protected String navStatus;
 	protected String eta = "N/A";
 	protected String posAcc = "N/A";
 	protected String source;
 	protected String pos;
 	protected PastTrack pastTrack = null;	
 
 	public DetailedAisTarget() {
 
 	}
 
 	public void init(AisVesselTarget aisVessel) {
 		AisVesselStatic aisVesselStatic = aisVessel.getAisVesselStatic();
 		if (aisVesselStatic == null) return;
 		AisVesselPosition aisVesselPosition = aisVessel.getAisVesselPosition();
 		if (aisVesselPosition == null) return;
 		AisClassAPosition aisClassAPosition = aisVesselPosition.getAisClassAPosition();
 		AisClassAStatic aisClassAStatic = aisVesselStatic.getAisClassAStatic();		
 		
 		
 		if (aisVesselStatic.getDimBow() != null && aisVesselStatic.getDimStern() != null) {
 			this.length = (short) (aisVesselStatic.getDimBow() + aisVesselStatic.getDimStern());
 		}
 		if (aisVesselStatic.getDimPort() != null && aisVesselStatic.getDimStarboard() != null) {
 			this.width = (byte) (aisVesselStatic.getDimPort() + aisVesselStatic.getDimStarboard());
 		}
 		
 		this.currentTime = System.currentTimeMillis();
 		this.id = aisVessel.getId();
 		this.mmsi = aisVessel.getMmsi();
 		this.vesselClass = aisVessel.getVesselClass();
 		this.lastReceived = formatTime(currentTime - aisVessel.getLastReceived().getTime());
 		this.lat = latToPrintable(aisVesselPosition.getLat());
 		this.lon = lonToPrintable(aisVesselPosition.getLon());
 		this.cog = formatDouble(aisVesselPosition.getCog(), 0);		
 		this.heading = formatDouble(aisVesselPosition.getHeading(), 1);
 		this.sog = formatDouble(aisVesselPosition.getSog(), 1);	
 		this.vesselType = aisVesselStatic.getShipTypeCargo().prettyType();
 
 		if (aisVessel.getCountry() != null) {
 			CountryMapper countryMapper = CountryMapper.getInstance();		
 			MidCountry midCountry = countryMapper.getByCode(aisVessel.getCountry()); 
 			if (midCountry != null) {
 				this.country = midCountry.getName();
 			}
 		} else {
 			this.country ="N/A";
 		}
 		
 		this.name = aisVesselStatic.getName();
 		this.callsign = aisVesselStatic.getCallsign();
 		this.cargo = aisVesselStatic.getShipTypeCargo().prettyCargo();
 		
 		this.source = aisVessel.getSource();
 
 		// Class A statics
 		if (aisClassAStatic != null) {
 			this.imoNo = Integer.toString(aisClassAStatic.getImo());
 			this.destination = aisClassAStatic.getDestination();
			this.draught = formatDouble((double)aisClassAStatic.getDraught() / 10.0, 1);
 			this.eta = getISO8620(aisClassAStatic.getEta());			
 		}		
 		
 		// Class A position
 		if (aisClassAPosition != null) {
 			NavigationalStatus navigationalStatus = new NavigationalStatus(aisClassAPosition.getNavStatus());
 			this.navStatus = navigationalStatus.prettyStatus();
 			this.moored = (aisClassAPosition.getNavStatus() == 1 || aisClassAPosition.getNavStatus() == 5);
 			this.rot = formatDouble(aisClassAPosition.getRot(), 1);
 		}
 				
 		if (aisVesselPosition.getPosAcc() == 1) {
 			this.posAcc = "High";
 		} else {
 			this.posAcc = "Low";
 		}
 		
 		this.pos = "N/A";
 		if (aisVesselPosition.getLat() != null && aisVesselPosition.getLon() != null) {
 			this.pos = latToPrintable(aisVesselPosition.getLat()) + " - " + lonToPrintable(aisVesselPosition.getLon());
 		}
 		
 	}
 	
 	public static String getISO8620(Date date) {
 		if (date == null) {
 			return "N/A";
 		}
 		SimpleDateFormat iso8601gmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 		iso8601gmt.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
 		return iso8601gmt.format(date);
 	}
 	
 	public static String formatTime(Long time) {
 		if (time == null) {
 			return "N/A";
 		}
 		long secondInMillis = 1000;
 		long minuteInMillis = secondInMillis * 60;
 		long hourInMillis = minuteInMillis * 60;
 		long dayInMillis = hourInMillis * 24;
 
 		long elapsedDays = time / dayInMillis;
 		time = time % dayInMillis;
 		long elapsedHours = time / hourInMillis;
 		time = time % hourInMillis;
 		long elapsedMinutes = time / minuteInMillis;
 		time = time % minuteInMillis;
 		long elapsedSeconds = time / secondInMillis;
 
 		if (elapsedDays > 0) {
 			return String.format("%02d:%02d:%02d:%02d", elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
 		} else if (elapsedHours > 0) {
 			return String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
 		} else {
 			return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
 		}
 	}
 	
 	public static String latToPrintable(Double lat) {
 		if (lat == null) {
 			return "N/A";
 		}
 		String ns = "N";
 		if (lat < 0) {
 			ns = "S";
 			lat *= -1;
 		}
 		int hours = (int)lat.doubleValue();
 		lat -= hours;
 		lat *= 60;
 		String latStr = String.format(Locale.US, "%3.3f", lat);
 		while (latStr.indexOf('.') < 2) {
 			latStr = "0" + latStr;
 		}		
 		return String.format(Locale.US, "%02d %s%s", hours, latStr, ns);
 	}
 	
 	public static String lonToPrintable(Double lon) {
 		if (lon == null) {
 			return "N/A";
 		}
 		String ns = "E";
 		if (lon < 0) {
 			ns = "W";
 			lon *= -1;
 		}
 		int hours = (int)lon.doubleValue();
 		lon -= hours;
 		lon *= 60;		
 		String lonStr = String.format(Locale.US, "%3.3f", lon);
 		while (lonStr.indexOf('.') < 2) {
 			lonStr = "0" + lonStr;
 		}		
 		return String.format(Locale.US, "%03d %s%s", hours, lonStr, ns);
 	}
 	
 	public static String formatDouble(Double d, int decimals) {
 		if (d == null) {
 			return "N/A";
 		}
 		if (decimals == 0) {
 			return String.format(Locale.US, "%d", Math.round(d));
 		}
 		String format = "%." + decimals + "f";
 		return String.format(Locale.US, format, d);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getCallsign() {
 		return callsign;
 	}
 
 	public void setCallsign(String callsign) {
 		this.callsign = callsign;
 	}
 
 	public String getImoNo() {
 		return imoNo;
 	}
 
 	public void setImoNo(String imoNo) {
 		this.imoNo = imoNo;
 	}
 
 	public String getCargo() {
 		return cargo;
 	}
 
 	public void setCargo(String cargo) {
 		this.cargo = cargo;
 	}
 
 	public String getCountry() {
 		return country;
 	}
 
 	public void setCountry(String country) {
 		this.country = country;
 	}
 
 	public String getDraught() {
 		return draught;
 	}
 
 	public void setDraught(String draught) {
 		this.draught = draught;
 	}
 
 	public String getRot() {
 		return rot;
 	}
 
 	public void setRot(String rot) {
 		this.rot = rot;
 	}
 
 	public String getDestination() {
 		return destination;
 	}
 
 	public void setDestination(String destination) {
 		this.destination = destination;
 	}
 
 	public String getNavStatus() {
 		return navStatus;
 	}
 
 	public void setNavStatus(String navStatus) {
 		this.navStatus = navStatus;
 	}
 
 	public String getEta() {
 		return eta;
 	}
 
 	public void setEta(String eta) {
 		this.eta = eta;
 	}
 
 	public String getPosAcc() {
 		return posAcc;
 	}
 
 	public void setPosAcc(String posAcc) {
 		this.posAcc = posAcc;
 	}
 	
 	public long getMmsi() {
 		return mmsi;
 	}
 	
 	public void setMmsi(long mmsi) {
 		this.mmsi = mmsi;
 	}
 	
 	public String getVesselClass() {
 		return vesselClass;
 	}
 	
 	public void setVesselClass(String vesselClass) {
 		this.vesselClass = vesselClass;
 	}
 
 	public String getLat() {
 		return lat;
 	}
 
 	public void setLat(String lat) {
 		this.lat = lat;
 	}
 
 	public String getLon() {
 		return lon;
 	}
 
 	public void setLon(String lon) {
 		this.lon = lon;
 	}
 
 	public boolean isMoored() {
 		return moored;
 	}
 	
 	public void setMoored(boolean moored) {
 		this.moored = moored;
 	}
 
 	public String getVesselType() {
 		return vesselType;
 	}
 
 	public void setVesselType(String vesselType) {
 		this.vesselType = vesselType;
 	}
 
 	public short getLength() {
 		return length;
 	}
 
 	public void setLength(short length) {
 		this.length = length;
 	}
 
 	public long getCurrentTime() {
 		return currentTime;
 	}
 
 	public void setCurrentTime(long currentTime) {
 		this.currentTime = currentTime;
 	}
 
 	public String getLastReceived() {
 		return lastReceived;
 	}
 
 	public void setLastReceived(String lastReceived) {
 		this.lastReceived = lastReceived;
 	}
 
 	public byte getWidth() {
 		return width;
 	}
 
 	public void setWidth(byte width) {
 		this.width = width;
 	}
 	
 	public String getSource() {
 		return source;
 	}
 	
 	public void setSource(String source) {
 		this.source = source;
 	}
 	
 	public PastTrack getPastTrack() {
 		return pastTrack;
 	}
 	
 	public void setPastTrack(PastTrack pastTrack) {
 		this.pastTrack = pastTrack;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getCog() {
 		return cog;
 	}
 
 	public void setCog(String cog) {
 		this.cog = cog;
 	}
 
 	public String getSog() {
 		return sog;
 	}
 
 	public void setSog(String sog) {
 		this.sog = sog;
 	}
 
 	public String getHeading() {
 		return heading;
 	}
 
 	public void setHeading(String heading) {
 		this.heading = heading;
 	}
 
 	public void setLength(Short length) {
 		this.length = length;
 	}
 
 	public void setWidth(Byte width) {
 		this.width = width;
 	}
 	
 	public String getPos() {
 		return pos;
 	}
 	
 	public void setPos(String pos) {
 		this.pos = pos;
 	}
 	
 }
