 package patterns.builder;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Builder Pattern Demo.
  */
 public class Builder {
 
     public static void main(String[] args){
         WeatherStation weatherStation = new WeatherStation(new XMLWeatherDataBuilder());
 
         System.out.print(weatherStation.getData());
     }
 
    /** "Director" */
     static class WeatherStation {
         private WeatherDataBuilder weatherDataBuilder;
 
         WeatherStation(WeatherDataBuilder weatherDataBuilder){
             super();
             this.weatherDataBuilder = weatherDataBuilder;
         }
 
         String getData(){
             this.weatherDataBuilder.addDate(new Date());
             this.weatherDataBuilder.addTemp(85);
             this.weatherDataBuilder.addWeatherType(Weather.WeatherType.SUNNY);
 
             return this.weatherDataBuilder.build();
         }
 
     }
 
     /**
     * Product
      */
     static class Weather {
 
         enum WeatherType {RAIN,SNOW,SUNNY,CLOUDY,UNKNOWN}
 
         private Date date = new Date();
         private int temp = 72;
         private WeatherType weatherType = WeatherType.UNKNOWN;
 
         Date getDate() {
             return date;
         }
 
         void setDate(Date date) {
             this.date = date;
         }
 
         int getTemp() {
             return temp;
         }
 
         void setTemp(int temp) {
             this.temp = temp;
         }
 
         WeatherType getWeatherType() {
             return weatherType;
         }
 
         void setWeatherType(WeatherType weatherType) {
             this.weatherType = weatherType;
         }
     }
 
     /**
     * Builder.
      */
     static abstract class WeatherDataBuilder {
 
         protected Weather weather = new Weather();
 
         WeatherDataBuilder addTemp(int temp){
             this.weather.setTemp(temp);
             return this;
         }
 
         WeatherDataBuilder addDate(Date date){
             this.weather.setDate(date);
             return this;
         }
 
         WeatherDataBuilder addWeatherType(Weather.WeatherType weatherType){
             this.weather.setWeatherType(weatherType);
             return this;
         }
 
         abstract String build();
     }
 
     /**
     * Concrete Builder.
      *
      * Build the Weather data in XML format.
      */
     static class XMLWeatherDataBuilder extends WeatherDataBuilder {
 
         private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 
         @Override
         String build() {
             StringBuilder sb = new StringBuilder();
             sb.append("<Weather date='"+this.dateFormat.format(this.weather.getDate())+"'>\n");
             sb.append("    <temp>"+this.weather.getTemp()+"</temp>\n");
             sb.append("    <current>"+this.weather.getWeatherType().toString()+"</current>\n");
             sb.append("<Weather>");
 
             return sb.toString();
         }
     }
 }
