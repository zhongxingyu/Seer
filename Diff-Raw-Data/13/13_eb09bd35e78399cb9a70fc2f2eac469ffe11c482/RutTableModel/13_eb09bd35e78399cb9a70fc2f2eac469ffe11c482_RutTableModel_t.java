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
 
 import cl.pucv.eii.jtaxi.interfaces.Observable;
 import cl.pucv.eii.jtaxi.interfaces.Observer;
 import cl.pucv.eii.jtaxi.modelo.Central;
 import cl.pucv.eii.jtaxi.modelo.Flota;
 import cl.pucv.eii.jtaxi.modelo.Pasajero;
 import cl.pucv.eii.jtaxi.modelo.Taxi;
 import cl.pucv.eii.jtaxi.modelo.Taxista;
 import cl.pucv.eii.jtaxi.utilidades.listas.Lista;
 import cl.pucv.eii.jtaxi.utilidades.listas.ListaDoble;
 import cl.pucv.eii.jtaxi.utilidades.rut.Rut;
 
 @SuppressWarnings("serial")
 public class RutTableModel extends AbstractTableModel implements Observer {
 
 	private ListaDoble<Rut> lista;
 	private Lista<Rut> ruts;
 	private Central central;
 
 	public RutTableModel(Observable o, Central central, ListaDoble<Rut> ruts) {
 		o.agregarObserver(this);
 		this.central = central;
 		this.ruts = ruts;
		updateLista();
 	}
 
 	@Override
 	public String getColumnName(int columnIndex) {
 		String[] columnName = { "RUT", "DV" };
 		return columnName[columnIndex];
 	}
 
 	@Override
 	public int getColumnCount() {
 		return 2;
 	}
 
 	@Override
 	public int getRowCount() {
 		return lista.tamaño();
 	}
 
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		Rut item = lista.getObject(rowIndex);
 		Object[] aux = { item.getNumero(), item.getDV() };
 		return aux[columnIndex];
 	}
 
 	private void updateLista() {
 		lista = new ListaDoble<Rut>();
 		ListaDoble<Flota> liFlotas = new ListaDoble<>();
 		ListaDoble<Pasajero> liPasa = new ListaDoble<>();
 		ListaDoble<Taxi> liTaxi = new ListaDoble<>();
 		ListaDoble<Taxista> liTta = new ListaDoble<>();
 		central.listarFlotas(liFlotas);
 		for (Flota f : liFlotas)
 			f.listarTaxista(liTta);
 		for (Taxista t : liTta)
 			lista.agregar(t.getRut());
 		for (Flota f : liFlotas)
 			f.listarTaxis(liTaxi);
 		for (Taxi t : liTaxi)
 			t.listarPasajeros(liPasa);
 		for (Pasajero p : liPasa)
 			lista.agregar(p.getRut());
 		for (Rut r : ruts)
 			lista.agregar(r);
 	}
 
 	@Override
 	public void actualizar(String estructura) {
 		if ("Rut".equals(estructura))
 			updateLista();
 	}
 
 }
