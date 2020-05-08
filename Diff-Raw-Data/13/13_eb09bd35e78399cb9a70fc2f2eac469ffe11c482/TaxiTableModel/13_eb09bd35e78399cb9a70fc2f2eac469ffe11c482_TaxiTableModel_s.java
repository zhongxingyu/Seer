 /*
  * Copyright (c) 2011, Julio Jiménez, René Toro, José Vargas. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * This file is part of JTaxi.
  * 
  * JTaxi is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTaxi is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with JTaxi.  If not, see <http://www.gnu.org/licenses/>.
  */
 package cl.pucv.eii.jtaxi.gui.tablemodels;
 
 import javax.swing.table.AbstractTableModel;
 
 import cl.pucv.eii.jtaxi.gui.ListaContadora;
 import cl.pucv.eii.jtaxi.interfaces.Observable;
 import cl.pucv.eii.jtaxi.interfaces.Observer;
 import cl.pucv.eii.jtaxi.modelo.Central;
 import cl.pucv.eii.jtaxi.modelo.Flota;
 import cl.pucv.eii.jtaxi.modelo.Paradero;
 import cl.pucv.eii.jtaxi.modelo.Taxi;
 import cl.pucv.eii.jtaxi.utilidades.listas.Lista;
 import cl.pucv.eii.jtaxi.utilidades.listas.ListaDoble;
 
 @SuppressWarnings("serial")
 public class TaxiTableModel extends AbstractTableModel implements Observer {
 
 	private Lista<Taxi> lista;
 	private Central central;
 	
 	public TaxiTableModel (Observable o, Central central){
 		o.agregarObserver(this);
 		this.central = central;
 	}
 	
 	@Override
 	public String getColumnName(int columnIndex){
 		String[] columnName = {"Taxista", "Patente", "Marca","Modelo","Nº Paraderos","Nº Pasajeros"};
 		return columnName[columnIndex];
 	}
 	
 	@Override
 	public int getColumnCount() {
 		return 5;
 	}
 
 	@Override
 	public int getRowCount() {
 		return lista.tamaño();
 	}
 
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		Taxi item = lista.getObject(rowIndex);
 		Flota f = central.buscarFlotaTaxi(item.getPatente());
 		ListaContadora<Paradero> nParaderos = new ListaContadora<>();
 		f.listarParaderos(nParaderos);
 		Object[] aux = {
 				(item.getTaxista() == null) ? "S/T" : item.getTaxista().getNombre(),
 				item.getPatente(),
 				item.getMarca(),
 				item.getModelo(),
 				nParaderos.tamaño(),
 				item.getNPasajeros()};
 		return aux[columnIndex];
 	}
 	
 	private void updateLista(){
 		lista = new ListaDoble<>();
 		ListaDoble<Flota> liFlotas = new ListaDoble<Flota>();
 		central.listarFlotas(liFlotas);
 		for (Flota f : liFlotas)
 			f.listarTaxis(lista);
 	}
 
 	@Override
 	public void actualizar(String estructura) {
 		if("Taxi".equals(estructura))
 			updateLista();
 	}
 
 }
