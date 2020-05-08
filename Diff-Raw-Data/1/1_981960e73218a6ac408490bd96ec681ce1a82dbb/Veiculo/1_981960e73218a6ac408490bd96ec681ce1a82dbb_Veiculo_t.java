 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Random;
 
 public abstract class Veiculo implements Serializable {
 
 	private String marca;
 	private String modelo;
 	private int cilindrada;
 	private int cv;
 	private Piloto p1;
 	private Piloto p2;
 	private boolean pactivo;
 	
 	private int voltas;
 
 	/*
 	 * Constructor for objects of class Veiculo
 	 */
 	public Veiculo() {
 		this.marca = "";
 		this.modelo = "";
 		this.cilindrada = 0;
 		this.cv = 0;
 		this.p1 = new Piloto();
 		this.p2 = new Piloto();
 		this.pactivo = true;	
 	}
 
 	public Veiculo(String marca, String modelo, int cilindrada, int cv, Piloto p1, Piloto p2) {
 		this.marca = marca;
 		this.modelo = modelo;
 		this.cilindrada = cilindrada;
 		this.cv = cv;
 		this.p1 = p1;
 		this.p2 = p2;
 		this.pactivo = true;
 	}
 	public Veiculo(Veiculo v) {
 		this.marca = v.getMarca();
 		this.modelo = v.getModelo();
 		this.cilindrada = v.getCilindrada();
 		this.cv = v.getCV();
 		this.p1 = v.getPiloto1();
 		this.p2 = v.getPiloto2();
 		this.pactivo = v.getPactivo();
		this.voltas = v.getVoltas();
 	}
 	
 	/*
 	 * Métodos de Instância
 	 */
 	public boolean getPactivo() {
 		return this.pactivo;
 	}
 
 	public int getPilotoActivo() {
 		int x = 0;
 		if (pactivo == true)
 			x = p1.getQualidade();
 		if (pactivo == false)
 			x = p2.getQualidade();
 		return x;
 	}
 
 	public String getMarca() {
 		return this.marca;
 	}
 
 	public String getModelo() {
 		return this.modelo;
 	}
 
 	public int getCilindrada() {
 		return this.cilindrada;
 	}
 
 	public int getCV() {
 		return this.cv;
 	}
 
 	public Piloto getPiloto1() {
 		return this.p1;
 	}
 
 	public Piloto getPiloto2() {
 		return this.p2;
 	}
 	
 	//SETTERS
 
 	public void setMarca(String marca) {
 		this.marca = marca;
 	}
 
 	public void setModelo(String modelo) {
 		this.modelo = modelo;
 	}
 
 	public void setCilindrada(int c) {
 		this.cilindrada = c;
 	}
 
 	public void setCV(int cv) {
 		this.cv = cv;
 	}
 
 	public void setPiloto1(Piloto p) {
 		this.p1 = p;
 	}
 
 	public void setPiloto(Piloto p) {
 		this.p2 = p;
 	}
 	
 	public void setPilotoActivo() {
 		if (pactivo == true)
 			pactivo = false;
 		if (pactivo == false)
 			pactivo = true;
 	}
 
 	public abstract Veiculo clone();
 
 	public abstract boolean equals(Object o) ;
 
 	public abstract String toString();
 
 	public abstract int tempoProximaVolta(Circuito c, boolean chuva) throws Exception;
 	
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		
 		result = prime * result + this.cilindrada;
 		result = prime * result + this.cv;
 		result = prime * result + ((this.marca == null) ? 0 : this.marca.hashCode());
 		result = prime * result + ((this.modelo == null) ? 0 : this.modelo.hashCode());
 		return result;
 	}
 	
 	public boolean ConducaoChuva() {
 		boolean x = false;
 		if (pactivo == true)
 			x = p1.getChuva();
 		if (pactivo == false)
 			x = p2.getChuva();
 		return x;
 	}
 	
 	public int getVoltas() {
 		return this.voltas;
 	}
 	
 	public void setVoltas(int n) {
 		this.voltas = n;
 	}
 	
 	public void voltaracio(int n) {
 		this.voltas = p1.getQualidade()*n/(p1.getQualidade()+p2.getQualidade());
 		
 	}
 	
 	public static String daMarca() {
 		Random r = new Random();
 		int x = r.nextInt(37);
 		String s = "";
 		
 		switch(x) {
             case 0: s = "Agrale"; break;
             case 1: s = "Aston Martin"; break;
             case 2: s = "Audi"; break;
             case 3: s = "Bentley"; break;
             case 4: s = "BMW"; break;
             case 5: s = "Changan"; break;
             case 6: s = "Chery"; break;
             case 7: s = "Chevrolet"; break;
             case 8: s = "Chrysler"; break;
             case 9: s = "Citroën"; break;
             case 10: s = "Dodge"; break;
             case 11: s = "Effa"; break;
             case 12: s = "Ferrari"; break;
             case 13: s = "Fiat"; break;
             case 14: s = "Ford"; break;
             case 15: s = "Hafei"; break;
             case 16: s = "Honda"; break;
             case 17: s = "Hyundai"; break;
             case 18: s = "Iveco"; break;
             case 19: s = "Jac Motors"; break;
             case 20: s = "Jaguar"; break;
             case 21: s = "Jeep"; break;
             case 22: s = "Jinbei"; break;
             case 23: s = "Kia"; break;
             case 24: s = "Lamborghini"; break;
             case 25: s = "Land Rover"; break;
             case 26: s = "Maserati"; break;
             case 27: s = "Mercedes-Benz"; break;
             case 28: s = "Mini"; break;
             case 29: s = "Nissan"; break;
             case 30: s = "Peugeot"; break;
             case 31: s = "Porsche"; break;
             case 32: s = "Renault"; break;
             case 33: s = "Subaru"; break;
             case 34: s = "Toyota"; break;
             case 35: s = "Volkswagen"; break;
             case 36: s = "Volvo"; break;
 		}
 		return s;
 	}
     
 	public static String daModelo() {
 		Random r = new Random();
 		int x = r.nextInt(50);
 		String s = "";
 		
 		switch(x) {
             case 0: s = "MiTO"; break;
             case 1: s = "Vantage"; break;
             case 2: s = "RS5"; break;
             case 3: s = "TT"; break;
             case 4: s = "Continental"; break;
             case 5: s = "Z4"; break;
             case 6: s = "Camaro"; break;
             case 7: s = "Corvette"; break;
             case 8: s = "Spark"; break;
             case 9: s = "C1"; break;
             case 10: s = "C-Crosser"; break;
             case 11: s = "Nemo"; break;
             case 12: s = "Duster"; break;
             case 13: s = "Journey"; break;
             case 14: s = "612 Scaglietti"; break;
             case 15: s = "F599"; break;
             case 16: s = "500"; break;
             case 17: s = "Strada"; break;
             case 18: s = "Punto"; break;
             case 19: s = "Fiesta"; break;
             case 20: s = "Transit Connect"; break;
             case 21: s = "S-Max"; break;
             case 22: s = "Civic"; break;
             case 23: s = "Jazz"; break;
             case 24: s = "Rock"; break;
             case 25: s = "Pop"; break;
             case 26: s = "Veloster"; break;
             case 27: s = "NLR"; break;
             case 28: s = "Daily"; break;
             case 29: s = "XKR"; break;
             case 30: s = "Grand Cherokee"; break;
             case 31: s = "Gallardo"; break;
             case 32: s = "Ypsilon"; break;
             case 33: s = "Discovery"; break;
             case 34: s = "Sport"; break;
             case 35: s = "Exige"; break;
             case 36: s = "Quattroporte"; break;
             case 37: s = "MX-5"; break;
             case 38: s = "SLK"; break;
             case 39: s = "Roadster"; break;
             case 40: s = "Outlander"; break;
             case 41: s = "NV200"; break;
             case 42: s = "Primastar"; break;
             case 43: s = "RCZ"; break;
             case 44: s = "Panamera"; break;
             case 45: s = "Twizy"; break;
             case 46: s = "Octavia"; break;
             case 47: s = "Impreza"; break;
             case 48: s = "GT 86"; break;
             case 49: s = "XC90"; break;
 		}
 		return s;
 	}
     
 	public static Veiculo geraHibrido(Piloto p1, Piloto p2) { //falta a ideia do griffin no fim
 		Veiculo v = null;
 		Random r = new Random();
 		int x = r.nextInt(3);
 		
 		if(x==0)
 			v= new PC1Hibrido(daMarca(), daModelo(), 6000, r.nextInt(600) + 700, p1, p2, r.nextInt(175) + 25);
 		if(x==1)
 			v= new PC2Hibrido(daMarca(), daModelo(), (r.nextInt(2000) + 4000), r.nextInt(400) + 550, p1, p2, r.nextInt(175) + 25);
 		if(x==2)
 			v= new GTHibrido(daMarca(), daModelo(), (r.nextInt(1500) + 3000), r.nextInt(200) + 400, p1, p2, r.nextInt(175) + 25);
 		return v;
 	}
     
 	public static Veiculo geraVeiculo() { //falta a ideia do griffin no fim
 		Piloto p1 = Piloto.geraPiloto();
 		Piloto p2 = Piloto.geraPiloto();
 		Veiculo v = null;
 		Random r = new Random();
 		int x = r.nextInt(5);
 		
 		if(x==0)
 			v= geraHibrido(p1,p2);
 		if(x==1)
 			v= new PC1Normal(daMarca(), daModelo(), 6000, r.nextInt(600) + 700, p1, p2);
 		if(x==2)
 			v= new PC2Normal(daMarca(), daModelo(), (r.nextInt(2000) + 4000), r.nextInt(400) + 550, p1, p2);
 		if(x==3)
 			v= new GTNormal(daMarca(), daModelo(), (r.nextInt(1500) + 3000), r.nextInt(200) + 400, p1, p2);
 		if(x==4)
 			v= new SC(daMarca(), daModelo(), 2500, r.nextInt(100) + 100, p1, p2);
 		
 		return v;
 	}
 	
 	public static HashSet<Veiculo> geraVeiculos() {
 		Random r = new Random();
 		int x = r.nextInt(15) + 12, i=0;
 		HashSet<Veiculo> aux = new HashSet<Veiculo>();
 		
 		while (i<x) {
 			aux.add(geraVeiculo());
 			i++;
 		}
 		return aux;
 	}
 	
 	public  boolean veHib(){
 		Class[] c =  this.getClass().getInterfaces();
 		int x = 0; String s = "interface Hibrida"; 
 		boolean k = false;
 		while(x <c.length) {
 			
 			if(c[x].toString().equals(s)) k =true;
 		
 		x++;
 		}
 		return k;
 		
 		
 	}
 }
