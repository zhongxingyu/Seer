 package Model;
 
 import Exceptions.AnoInvalidoException;
 import Exceptions.DiaInvalidoException;
 import Exceptions.MesInvalidoException;
 
 public class Data implements Comparable<Data> {
 
 	private int dia;
 	private int ano;
 	private int mes;
 
 	public Data(int dia, int mes, int ano) throws DiaInvalidoException,
 			MesInvalidoException, AnoInvalidoException {
 		if (dia < 1 || dia > 31)
 			throw new DiaInvalidoException();
 		if (mes == 2 && dia > 29)
 			throw new MesInvalidoException();
 		if (mes == 2 && dia == 29) {
 			if (!(ano % 4 == 0)) {
 				throw new AnoInvalidoException();
 			} else {
 				if ((ano % 100 == 0))
 					throw new AnoInvalidoException();
 			}
 		}
 		if (dia == 31) {
 			if (mes == 2 || mes == 4 || mes == 6 || mes == 9 || mes == 11)
 				throw new MesInvalidoException();
 		}		
 
 		this.dia = dia;
 		this.mes = mes;
 		this.ano = ano;
 	}
 
 	public Data() {
 	}
 
 	public int getDia() {
 		return dia;
 	}
 
 	public void setDia(int dia) throws DiaInvalidoException {
 		if (dia >= 1 && dia <= 31) {
 			this.dia = dia;
 		} else {
 			throw new DiaInvalidoException();
 		}
 	}
 
 	public int getAno() {
 		return ano;
 	}
 
 	public void setAno(int ano) throws AnoInvalidoException {
 		if (ano > 1900) {
 			this.ano = ano;
 		} else {
 			throw new AnoInvalidoException();
 		}
 	}
 
 	public int getMes() {
 		return mes;
 	}
 
 	public void setMes(int mes) throws MesInvalidoException {
 		if (mes >= 1 && mes <= 12) {
 			this.mes = mes;
 		} else {
 			throw new MesInvalidoException();
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ano;
 		result = prime * result + dia;
 		result = prime * result + mes;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Data))
 			return false;
 		return this.getDia() == ((Data) obj).getDia()
 				&& this.getMes() == ((Data) obj).getMes()
 				&& this.getAno() == ((Data) obj).getAno();
 	}
 
 	@Override
 	public int compareTo(Data data) {
 		if (this.getAno() == 0 && this.getMes() == 0 && this.getDia() == 0)
 			return 2;
		if(data.getAno() == 0 && data.getMes() == 0 && data.getDia() == 0) 
			return -2;
 		if (data.getAno() > this.getAno()) {
 			return -1;
 		} else if (data.getAno() < this.getAno()) {
 			return 1;
 		} else {
 			if (data.getMes() > this.getMes()) {
 				return -1;
 			} else if (data.getMes() < this.getMes()) {
 				return 1;
 			} else {
 				if (data.getDia() > this.getDia()) {
 					return -1;
 				} else if (data.getDia() < this.getDia()) {
 					return 1;
 				}
 			}
 		}
 		return 0;
 	}
 
 	@Override
 	public String toString() {
 		return this.getDia() + "/" + this.getMes() + "/" + this.getAno();
 	}
 }
