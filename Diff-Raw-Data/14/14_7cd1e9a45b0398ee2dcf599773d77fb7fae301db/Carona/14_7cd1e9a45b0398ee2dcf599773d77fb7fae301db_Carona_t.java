 package model;
 
 import org.joda.time.DateTime;
 
 public class Carona {
 
 	private String origem;
 	private String destino;
 	private DateTime dateTime;
 	private int vagas,year,month,day,hour,minute;
 
 
 
 	public Carona(String origem, String destino, String data,String horaSaida, int vagas) {
 		if(Util.isEmpty(origem) || Util.isEmpty(origem)|| Util.isEmpty(origem) || Util.isEmpty(origem) || vagas < 1){
 			throw new RuntimeException("Parametros incosistentes");
 		}
 		year = Integer.parseInt(data.split("/")[2]);
 		month = Integer.parseInt(data.split("/")[1]);
 		day = Integer.parseInt(data.split("/")[0]);
 		hour =Integer.parseInt(horaSaida.split(":")[0]);
 		minute = Integer.parseInt(horaSaida.split(":")[1]);
 
 		this.dateTime = new DateTime(year, month, day, hour, minute);
 		this.origem = origem;
 		this.vagas=vagas;
 		this.destino = destino;
 	}
 
 	@Override
 	public boolean equals(Object obj){
 		if(!(obj instanceof Carona)){
 			return false;
 		}
 		Carona carona = (Carona) obj;
 		return ( carona.getOrigem().equals(this.getOrigem())
 				 && carona.getDestino().equals(this.getDestino())
 				 && carona.getDate().equals(this.getDate())
 				 && carona.getHour().equals(this.getHour()) 
 				 && carona.getVagas() == this.getVagas() );
 	}
 	
 	public String getTrajeto(){
 		return origem+" - "+destino;
 	}
 	
 	
 	public String getOrigem() {
 		return origem;
 	}
 
 
 
 	public void setOrigem(String origem) {
 		this.origem = origem;
 	}
 
 
 
 	public String getDestino() {
 		return destino;
 	}
 
 
 
 	public void setDestino(String destino) {
 		this.destino = destino;
 	}
 
 
 	public String getDate(){
 		return dateTime.toString("dd/MM/yyyy");
 	}
 
 
 	public String getHour() {
		return dateTime.toString("HH:mm");
 	}
 
 	public void setDate(String data) {
 		this.year = Integer.parseInt(data.split("/")[2]);
 		this.month = Integer.parseInt(data.split("/")[1]);
 		this.day = Integer.parseInt(data.split("/")[0]);
 		this.dateTime = new DateTime(year, month, day, hour, minute);
 	}
 
 	public void setHour(String hora){
 		this.hour =Integer.parseInt(hora.split(":")[0]);
 		this.minute = Integer.parseInt(hora.split(":")[1]);
 		this.dateTime = new DateTime(year, month, day, hour, minute);
 	}
 
 
 	public int getVagas() {
 		return vagas;
 	}
 
 
 
 	public void setVagas(int vagas) {
 		this.vagas = vagas;
 	}
 
 	@Override
 	public String toString() {
 		return origem +" para "+ destino+", no dia "+getDate()+", as "+getHour();
 		
 		//"João Pessoa para Campina Grande, no dia 25/11/2026, as 06:59"
 	}
 
public static void main(String[] args) {
	Carona carona = new Carona("as", "as", "10/10/2013", "6:12", 3);
	System.out.println(carona.getHour());
}
 
 
 }
