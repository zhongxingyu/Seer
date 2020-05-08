 /**
  * CountryInfo.java
  *
  * This file was auto-generated from WSDL
  * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
  */
 
 package org.webservice.countryInfo;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.URL;
 import java.rmi.RemoteException;
 
 import javax.jws.WebMethod;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.axis.message.MessageElement;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import NET.webserviceX.www.CountrySoapProxy;
 import NET.webserviceX.www.GlobalWeatherSoapProxy;
 
 import com.ezzylearning.www.services.CountryInformationService_asmx.CountryInformationServiceSoapProxy;
 import com.ezzylearning.www.services.CountryInformationService_asmx.GetCountriesResponseGetCountriesResult;
 
 public class CountryInfo{
 	
 	private final String EOL=System.getProperty("line.separator");
 	private final String GEONAMES_USER_ACCOUNT="balhau";
 	private CountryInfoResult res;
 	private String countryCode;
 	
 	private CountryInfoKV[] getCountriesMap(){
 		CountryInfoKV[] cNInfo=null;
 		GetCountriesResponseGetCountriesResult res=null;
 		CountryInformationServiceSoapProxy proxy1=new CountryInformationServiceSoapProxy();
 		proxy1.setEndpoint(proxy1.getEndpoint());
 		try {
 			res=proxy1.getCountries();
 		} catch (RemoteException e) {
 			return null;
 		}
 		MessageElement maux;
 		NodeList nla1,nla2;
 		int laux;
 		MessageElement[] msg=null;
 		msg=res.get_any();
 		if(msg!=null){
 			for(int i=0;i<msg.length;i++){
 				NodeList lst=msg[i].getElementsByTagName("Countries");
 				laux=lst.getLength();
 				cNInfo=new CountryInfoKV[laux];
 				for(int j=0;j<laux;j++){
 					maux=(MessageElement)lst.item(j);
 					nla1=maux.getElementsByTagName("Country");
 					nla2=maux.getElementsByTagName("ISO2");
 					cNInfo[j]=new CountryInfoKV(
 							((MessageElement)nla1.item(0)).getValue(),
 							((MessageElement)nla2.item(0)).getValue()
 							);
 				}
 			}
 		}
 		return cNInfo;
 	}
 	
 	private String getGeoNameCountryInfoURL(String iso2Code){
 		return "http://api.geonames.org/countryInfo?lang="+iso2Code+"&country="+iso2Code+"&username="+GEONAMES_USER_ACCOUNT+"&style=full";
 	}
 	
 	private String randomCityWeatherName(String country) throws ParserConfigurationException, SAXException, IOException{
 		String cityName="";
 		GlobalWeatherSoapProxy proxy=new GlobalWeatherSoapProxy();
 		proxy.setEndpoint(proxy.getEndpoint());
 		String xml=proxy.getCitiesByCountry(country);
 		Document dom=xml2dom(xml);
 		NodeList els=dom.getElementsByTagName("Table");
 		int maxEl=els.getLength();
 		int sel=(int)Math.floor(Math.random()*maxEl);
 		Element el=(Element)els.item(sel);
 		els=el.getElementsByTagName("City");
 		cityName=els.item(0).getFirstChild().getNodeValue();
 		return cityName;
 	}
 	
 	private String getURL(String url){
 	        URL u;
 	        StringBuilder sb=new StringBuilder();
 	        BufferedReader in=null;
 			try {
 				u = new URL(url);
 	        in = new BufferedReader(
 	        new InputStreamReader(u.openStream()));
 
 	        String inputLine;
 	        while ((inputLine = in.readLine()) != null)
 	        	sb.append(inputLine);
 	        in.close();
 			}catch(Exception e){}
 			finally{
 				try {
 					if(in!=null)in.close();
 				} catch (IOException e) {}
 			}
 			return sb.toString();
 	}
 	
 	private Document xml2dom(String xml) throws ParserConfigurationException, SAXException, IOException{
 		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
 		InputSource is = new InputSource();
         is.setCharacterStream(new StringReader(xml));
 		DocumentBuilder db=dbf.newDocumentBuilder();
 		return db.parse(is);
 	}
 	
 	private Element geoXML2Element(String xml) throws SAXException, IOException, ParserConfigurationException{
 		Document dom=xml2dom(xml);
 		NodeList nl=dom.getChildNodes();
 		Element geonames=(Element)nl.item(0);
 		return geonames;
 	}
 	
 	private String getCountryNameByIsoCode(CountryInfoKV[] pairs,String isoCode){
 		String out="";
 		for(int i=0;i<pairs.length;i++){
 			if(pairs[i].countryISO2.toLowerCase().equals(isoCode.toLowerCase()))
 				return pairs[i].countryName;
 		}
 		return "";
 	}
 	
 	private String getElVal(Element elroot,String tag){
 		Element aux=(Element)(elroot.getElementsByTagName(tag).item(0));
 		return aux.getFirstChild().getNodeValue();
 	}
 	
 	private interface IWork{
 		public void doJob(CountryInfoResult res,CountryInfoKV[] map,String countryCode);
 	}
 	
 	private class ServiceThread extends Thread{
 		private IWork job;
 		private CountryInfoKV map[];
 		private String countryCode;
 		private CountryInfoResult res;
 		public ServiceThread(CountryInfoResult res,CountryInfoKV[] map,String countryCode,IWork job){
 			this.job=job;
 			this.map=map;
 			this.res=res;
 			this.countryCode=countryCode;
 		}
 		
 		public void run(){
 			this.job.doJob(res,map,countryCode);
 		}
 	}
 	
 	@WebMethod
 	public CountryInfoResult getCountryInfo(String countryCode){
 		CountryInfoKV[] cmap=getCountriesMap();
 		this.countryCode=countryCode;
 		res = new CountryInfoResult();
 		res.ICAO="Please wait for the version 1.0.0.0.1";
 		ServiceThread[] threads=new ServiceThread[5];
 		
 		threads[0]=new ServiceThread(res,cmap,countryCode,new IWork() {
 			@Override
 			public void doJob(CountryInfoResult res,CountryInfoKV[] map,String countryCode) {
 				// TODO Auto-generated method stub
 				String data=getURL(getGeoNameCountryInfoURL(countryCode));
 				Element geodata=null;
 				try {
 					geodata=geoXML2Element(data);
 				} catch (SAXException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ParserConfigurationException e) {
 					e.printStackTrace();
 				}
 				res.Populacao=getElVal(geodata,"population");
 				res.Lingua=getElVal(geodata,"languages");
 				res.Capital=getElVal(geodata, "capital");
 				res.Area=getElVal(geodata,"areaInSqKm");
 				res.GeoInfo="W: "+getElVal(geodata,"west")+", N: "+getElVal(geodata, "north");
 			}
 		});
 		
 		threads[1]=new ServiceThread(res,cmap,countryCode,new IWork() {
 			
 			@Override
 			public void doJob(CountryInfoResult res,CountryInfoKV[] map,String countryCode) {
 				String cityWeather="";
 				String countryName=getCountryNameByIsoCode(map, countryCode);
 				try{cityWeather=randomCityWeatherName(countryName);}catch(Exception e){}
 				GlobalWeatherSoapProxy proxy=new GlobalWeatherSoapProxy();
 				proxy.setEndpoint(proxy.getEndpoint());
 				String weatherInfo="";
 				try {
 					weatherInfo=proxy.getWeather(cityWeather, countryName);
 				} catch (RemoteException e) {
 				}
 				res.RandomCityWeather=weatherInfo;
 			}
 		});
 		
 		threads[2]=new ServiceThread(res,cmap,countryCode,new IWork() {
 			
 			@Override
 			public void doJob(CountryInfoResult res,CountryInfoKV[] map,String countryCode) {
 				String countryName=getCountryNameByIsoCode(map, countryCode);
 				CountrySoapProxy proxy1=new CountrySoapProxy();
 				proxy1.setEndpoint(proxy1.getEndpoint());
 				String isd="";
 				try {
 					isd = proxy1.getISD(countryName);
 					Document d=xml2dom(isd);
 					Element root=(Element)d.getChildNodes().item(0);
 					res.ISD=getElVal(root, "code");
 				} catch (Exception e) {
 				}
 			}
 		});
 		//thread 3
 		threads[3]=new ServiceThread(res,cmap, countryCode, new IWork() {
 			
 			@Override
 			public void doJob(CountryInfoResult res,CountryInfoKV[] map, String countryCode) {
 				String countryName=getCountryNameByIsoCode(map, countryCode);
 				CountrySoapProxy proxy2=new CountrySoapProxy();
 				proxy2.setEndpoint(proxy2.getEndpoint());
 				String gmt="";
 				try{
 					gmt=proxy2.getGMTbyCountry(countryName);
 					Document d=xml2dom(gmt);
 					Element root=(Element)d.getChildNodes().item(0);
 					res.GMT=getElVal(root,"GMT");
 				}catch(Exception e){}
 			}
 		});
 		
 		
 		threads[4]=new ServiceThread(res,cmap, countryCode, new IWork() {
 			@Override
 			public void doJob(CountryInfoResult res,CountryInfoKV[] map, String countryCode) {
 				String countryName=getCountryNameByIsoCode(map, countryCode);
 				CountrySoapProxy proxy3=new CountrySoapProxy();
 				proxy3.setEndpoint(proxy3.getEndpoint());
 				String currency="";
 				try{
 					currency=proxy3.getCurrencyByCountry(countryName);
 					Document d=xml2dom(currency);
 					Element root=(Element)d.getChildNodes().item(0);
 					res.Moeda=getElVal(root, "Currency")+"/"+getElVal(root, "CurrencyCode");
 				}catch(Exception e){}
 			}
 		});
 		
 		for(int i=0;i<threads.length;i++){
			threads[i].start();
 		}
 		for(int i=0;i<threads.length;i++){
 			try {
 				threads[i].join();
 			} catch (Exception e) {
 			}
 		}
 		return res;
 	}
 }
