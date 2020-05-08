 package nl.pvanassen.highchart;
 
 import java.util.Calendar;
 
 import nl.pvanassen.highchart.api.ChartOptions;
 import nl.pvanassen.highchart.api.Point;
 import nl.pvanassen.highchart.api.Series;
 import nl.pvanassen.highchart.api.format.DateTimeLabelFormats.TimeUnit;
 import nl.pvanassen.highchart.api.shared.SeriesType;
 
 import org.junit.Test;
 import org.junit.Assert;
import org.junit.Ignore;
 
 public class TestSplineIrregularTime {
     private static final String splineJson = "{\"chart\":{\"width\":800,\"height\":600,\"defaultSeriesType\":\"spline\",\"marginLeft\":70,\"marginTop\":80},\"plotOptions\":{\"area\":{\"borderWidth\":0},\"areaspline\":{\"borderWidth\":0},\"line\":{\"borderWidth\":0},\"pie\":{\"dataLabels\":{\"color\":\"#000000\",\"enabled\":true,\"formatter\":\"function() {return \\u0027\\u003cb\\u003e\\u0027+ this.point.name +\\u0027\\u003c/b\\u003e: \\u0027+ this.y +\\u0027 %\\u0027;}\",\"align\":\"center\",\"rotation\":0.0},\"allowPointSelect\":true,\"borderWidth\":0},\"series\":{\"borderWidth\":0},\"spline\":{\"borderWidth\":0},\"column\":{\"borderWidth\":0},\"bar\":{\"borderWidth\":0}},\"series\":[{\"data\":[{\"x\":2.58732E10,\"y\":0.0},{\"x\":2.70828E10,\"y\":0.6},{\"x\":2.7774E10,\"y\":0.7},{\"x\":2.89836E10,\"y\":0.8},{\"x\":2.95884E10,\"y\":0.6},{\"x\":3.01932E10,\"y\":0.6},{\"x\":3.123E10,\"y\":0.67},{\"x\":3.15756E10,\"y\":0.81},{\"x\":3.21804E10,\"y\":0.78},{\"x\":3.2526E10,\"y\":0.98},{\"x\":3.3822E10,\"y\":1.84},{\"x\":3.50316E10,\"y\":1.8},{\"x\":3.57228E10,\"y\":1.8},{\"x\":3.62412E10,\"y\":1.92},{\"x\":3.69324E10,\"y\":2.49},{\"x\":3.75372E10,\"y\":2.79},{\"x\":3.78828E10,\"y\":2.73},{\"x\":3.87468E10,\"y\":2.61},{\"x\":3.9438E10,\"y\":2.76},{\"x\":3.97836E10,\"y\":2.82},{\"x\":4.03884E10,\"y\":2.8},{\"x\":4.21164E10,\"y\":2.1},{\"x\":4.41036E10,\"y\":1.1},{\"x\":4.53132E10,\"y\":0.25},{\"x\":4.55724E10,\"y\":0.0}],\"name\":\"Winter 2007-2008\"},{\"data\":[{\"x\":2.50956E10,\"y\":0.0},{\"x\":2.57868E10,\"y\":0.2},{\"x\":2.88972E10,\"y\":0.47},{\"x\":2.97612E10,\"y\":0.55},{\"x\":3.09708E10,\"y\":1.38},{\"x\":3.21804E10,\"y\":1.38},{\"x\":3.27852E10,\"y\":1.38},{\"x\":3.4254E10,\"y\":1.38},{\"x\":3.48588E10,\"y\":1.48},{\"x\":3.5982E10,\"y\":1.5},{\"x\":3.76236E10,\"y\":1.89},{\"x\":3.87468E10,\"y\":2.0},{\"x\":3.96108E10,\"y\":1.94},{\"x\":4.00428E10,\"y\":1.91},{\"x\":4.03884E10,\"y\":1.75},{\"x\":4.09068E10,\"y\":1.6},{\"x\":4.40172E10,\"y\":0.6},{\"x\":4.45356E10,\"y\":0.35},{\"x\":4.51404E10,\"y\":0.0}],\"name\":\"Winter 2008-2009\"},{\"data\":[{\"x\":2.4318E10,\"y\":0.0},{\"x\":2.475E10,\"y\":0.15},{\"x\":2.8638E10,\"y\":0.35},{\"x\":2.98476E10,\"y\":0.46},{\"x\":3.15756E10,\"y\":0.59},{\"x\":3.35628E10,\"y\":0.58},{\"x\":3.4254E10,\"y\":0.62},{\"x\":3.47724E10,\"y\":0.65},{\"x\":3.61548E10,\"y\":0.77},{\"x\":3.7278E10,\"y\":0.77},{\"x\":3.77964E10,\"y\":0.79},{\"x\":3.86604E10,\"y\":0.86},{\"x\":3.96108E10,\"y\":0.8},{\"x\":4.08204E10,\"y\":0.94},{\"x\":4.13388E10,\"y\":0.9},{\"x\":4.32396E10,\"y\":0.39},{\"x\":4.36716E10,\"y\":0.0}],\"name\":\"Winter 2009-2010\"}],\"title\":{\"text\":\"Snow depth in the Vikjafjellet mountain, Norway\"},\"subtitle\":{\"text\":\"An example of irregular time data in Highcharts JS\"},\"xAxis\":{\"type\":\"datetime\",\"dateTimeLabelFormats\":{\"month\":\"%e. %b\",\"year\":\"%b\"}},\"yAxis\":{\"min\":0.0,\"title\":{\"text\":\"Snow depth (m)\"}}}";
 
    @Ignore
     @Test
     public void testSplineIrregularTime() {
         // http://highcharts.com/demo/spline-irregular-time
         ChartOptions chartOptions = new ChartOptions();
         chartOptions.getChart().setWidth( 800 ).setHeight( 600 ).setDefaultSeriesType( SeriesType.SPLINE ).setMarginLeft( 70 ).setMarginTop( 80 );
 
         // titles
         chartOptions.getTitle().setText( "Snow depth in the Vikjafjellet mountain, Norway" );
         chartOptions.getSubtitle().setText( "An example of irregular time data in Highcharts JS" );
 
         // axis
         chartOptions.getXAxis().setType( "datetime" ).getDateTimeLabelFormats().set( TimeUnit.MONTH, "%e. %b" ).set( TimeUnit.YEAR, "%b" );
         chartOptions.getYAxis().setMin( 0 ).getTitle().setText( "Snow depth (m)" );
 
         // plotOptions
         chartOptions.getPlotOptions().getPie().setAllowPointSelect( true ).getDataLabels().setEnabled( true ).setColor( "#000000" ).setFormatter( "function() {return '<b>'+ this.point.name +'</b>: '+ this.y +' %';}" );
 
         Series newSeries = new Series().setName( "Winter 2007-2008" );
         chartOptions.getSeries().pushElement( newSeries );
         newSeries.getData().pushElement( new Point().setX( getDateUTC( 1970, 9, 27 ) ).setY( 0 ) ).pushElement( new Point().setX( getDateUTC( 1970, 10, 10 ) ).setY( 0.6 ) ).pushElement( new Point().setX( getDateUTC( 1970, 10, 18 ) ).setY( 0.7 ) ).pushElement( new Point().setX( getDateUTC( 1970, 11, 2 ) ).setY( 0.8 ) ).pushElement( new Point().setX( getDateUTC( 1970, 11, 9 ) ).setY( 0.6 ) ).pushElement( new Point().setX( getDateUTC( 1970, 11, 16 ) ).setY( 0.6 ) ).pushElement( new Point().setX( getDateUTC( 1970, 11, 28 ) ).setY( 0.67 ) ).pushElement( new Point().setX( getDateUTC( 1971, 0, 1 ) ).setY( 0.81 ) ).pushElement( new Point().setX( getDateUTC( 1971, 0, 8 ) ).setY( 0.78 ) ).pushElement( new Point().setX( getDateUTC( 1971, 0, 12 ) ).setY( 0.98 ) ).pushElement( new Point().setX( getDateUTC( 1971, 0, 27 ) ).setY( 1.84 ) ).pushElement( new Point().setX( getDateUTC( 1971, 1, 10 ) ).setY( 1.8 ) ).pushElement( new Point().setX( getDateUTC( 1971, 1, 18 ) ).setY( 1.8 ) ).pushElement( new Point().setX( getDateUTC( 1971, 1, 24 ) ).setY( 1.92 ) ).pushElement( new Point().setX( getDateUTC( 1971, 2, 4 ) ).setY( 2.49 ) ).pushElement( new Point().setX( getDateUTC( 1971, 2, 11 ) ).setY( 2.79 ) ).pushElement( new Point().setX( getDateUTC( 1971, 2, 15 ) ).setY( 2.73 ) ).pushElement( new Point().setX( getDateUTC( 1971, 2, 25 ) ).setY( 2.61 ) ).pushElement( new Point().setX( getDateUTC( 1971, 3, 2 ) ).setY( 2.76 ) ).pushElement( new Point().setX( getDateUTC( 1971, 3, 6 ) ).setY( 2.82 ) ).pushElement( new Point().setX( getDateUTC( 1971, 3, 13 ) ).setY( 2.8 ) ).pushElement( new Point().setX( getDateUTC( 1971, 4, 3 ) ).setY( 2.1 ) ).pushElement( new Point().setX( getDateUTC( 1971, 4, 26 ) ).setY( 1.1 ) ).pushElement( new Point().setX( getDateUTC( 1971, 5, 9 ) ).setY( 0.25 ) ).pushElement( new Point().setX( getDateUTC( 1971, 5, 12 ) ).setY( 0 ) );
 
         newSeries = new Series().setName( "Winter 2008-2009" );
         chartOptions.getSeries().pushElement( newSeries );
         newSeries.getData().pushElement( new Point().setX( getDateUTC( 1970, 9, 18 ) ).setY( 0 ) ).pushElement( new Point().setX( getDateUTC( 1970, 9, 26 ) ).setY( 0.2 ) ).pushElement( new Point().setX( getDateUTC( 1970, 11, 1 ) ).setY( 0.47 ) ).pushElement( new Point().setX( getDateUTC( 1970, 11, 11 ) ).setY( 0.55 ) ).pushElement( new Point().setX( getDateUTC( 1970, 11, 25 ) ).setY( 1.38 ) ).pushElement( new Point().setX( getDateUTC( 1971, 0, 8 ) ).setY( 1.38 ) ).pushElement( new Point().setX( getDateUTC( 1971, 0, 15 ) ).setY( 1.38 ) ).pushElement( new Point().setX( getDateUTC( 1971, 1, 1 ) ).setY( 1.38 ) ).pushElement( new Point().setX( getDateUTC( 1971, 1, 8 ) ).setY( 1.48 ) ).pushElement( new Point().setX( getDateUTC( 1971, 1, 21 ) ).setY( 1.5 ) ).pushElement( new Point().setX( getDateUTC( 1971, 2, 12 ) ).setY( 1.89 ) ).pushElement( new Point().setX( getDateUTC( 1971, 2, 25 ) ).setY( 2.0 ) ).pushElement( new Point().setX( getDateUTC( 1971, 3, 4 ) ).setY( 1.94 ) ).pushElement( new Point().setX( getDateUTC( 1971, 3, 9 ) ).setY( 1.91 ) ).pushElement( new Point().setX( getDateUTC( 1971, 3, 13 ) ).setY( 1.75 ) ).pushElement( new Point().setX( getDateUTC( 1971, 3, 19 ) ).setY( 1.6 ) ).pushElement( new Point().setX( getDateUTC( 1971, 4, 25 ) ).setY( 0.6 ) ).pushElement( new Point().setX( getDateUTC( 1971, 4, 31 ) ).setY( 0.35 ) ).pushElement( new Point().setX( getDateUTC( 1971, 5, 7 ) ).setY( 0 ) );
 
         newSeries = new Series().setName( "Winter 2009-2010" );
         chartOptions.getSeries().pushElement( newSeries );
         newSeries.getData().pushElement( new Point().setX( getDateUTC( 1970, 9, 9 ) ).setY( 0 ) ).pushElement( new Point().setX( getDateUTC( 1970, 9, 14 ) ).setY( 0.15 ) ).pushElement( new Point().setX( getDateUTC( 1970, 10, 28 ) ).setY( 0.35 ) ).pushElement( new Point().setX( getDateUTC( 1970, 11, 12 ) ).setY( 0.46 ) ).pushElement( new Point().setX( getDateUTC( 1971, 0, 1 ) ).setY( 0.59 ) ).pushElement( new Point().setX( getDateUTC( 1971, 0, 24 ) ).setY( 0.58 ) ).pushElement( new Point().setX( getDateUTC( 1971, 1, 1 ) ).setY( 0.62 ) ).pushElement( new Point().setX( getDateUTC( 1971, 1, 7 ) ).setY( 0.65 ) ).pushElement( new Point().setX( getDateUTC( 1971, 1, 23 ) ).setY( 0.77 ) ).pushElement( new Point().setX( getDateUTC( 1971, 2, 8 ) ).setY( 0.77 ) ).pushElement( new Point().setX( getDateUTC( 1971, 2, 14 ) ).setY( 0.79 ) ).pushElement( new Point().setX( getDateUTC( 1971, 2, 24 ) ).setY( 0.86 ) ).pushElement( new Point().setX( getDateUTC( 1971, 3, 4 ) ).setY( 0.8 ) ).pushElement( new Point().setX( getDateUTC( 1971, 3, 18 ) ).setY( 0.94 ) ).pushElement( new Point().setX( getDateUTC( 1971, 3, 24 ) ).setY( 0.9 ) ).pushElement( new Point().setX( getDateUTC( 1971, 4, 16 ) ).setY( 0.39 ) ).pushElement( new Point().setX( getDateUTC( 1971, 4, 21 ) ).setY( 0 ) );
 
         String json = chartOptions.toJson();
 
         Assert.assertEquals( "Expected spline json", splineJson, json );
     }
 
     private static long getDateUTC( int year, int month, int day ) {
         Calendar cal = Calendar.getInstance();
         cal.set( Calendar.YEAR, year );
         cal.set( Calendar.MONTH, month );
         cal.set( Calendar.DAY_OF_MONTH, day );
         cal.set( Calendar.HOUR, 0 );
         cal.set( Calendar.MINUTE, 0 );
         cal.set( Calendar.SECOND, 0 );
         cal.set( Calendar.MILLISECOND, 0 );
         return cal.getTimeInMillis();
     }
 }
