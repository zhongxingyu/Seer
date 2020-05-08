 package org.tamarillo.addressws.csv;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.io.StringReader;
 
 import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.LineIterator;
 import org.jsefa.Deserializer;
 import org.jsefa.csv.CsvIOFactory;
 import org.tamarillo.addressws.csv.model.ConcelhoCsv;
 import org.tamarillo.addressws.csv.model.DistritoCsv;
 import org.tamarillo.addressws.csv.model.TodosCodigosPostaisCsv;
 import org.tamarillo.addressws.entity.Arteria;
 import org.tamarillo.addressws.entity.ArteriaPK;
 import org.tamarillo.addressws.entity.Concelho;
 import org.tamarillo.addressws.entity.ConcelhoPK;
 import org.tamarillo.addressws.entity.Distrito;
 import org.tamarillo.addressws.entity.Localidade;
 import org.tamarillo.addressws.entity.LocalidadePK;
 import org.tamarillo.addressws.jpa.exception.PreexistingEntityException;
 import org.tamarillo.addressws.service.impl.AddressService;
 
 
 /**
  * The Class CsvReader.
  */
 @Named
@SessionScoped
 public class CsvReader implements Serializable {
 
 	/** The Constant serialVersionUID. */
 	private static final long serialVersionUID = -6574521032990428214L;
 
 	/** The address service. */
 	@Inject
 	private AddressService addressService;
 
 	/**
 	 * Handle distrito csv file.
 	 * 
 	 * @param is
 	 *            the is
 	 * @throws Exception 
 	 * @throws PreexistingEntityException 
 	 */
 	public void handleDistritoCsvFile(InputStream is) throws Exception {
 		LineIterator lineItr = IOUtils.lineIterator(is, "UTF-8");
 		Deserializer deserializer = CsvIOFactory.createFactory(
 				DistritoCsv.class).createDeserializer();
 		try {
 			while (lineItr.hasNext()) {
 				String line = lineItr.nextLine();
 				StringReader reader = new StringReader(line);
 				deserializer.open(reader);
 				while (deserializer.hasNext()) {
 					DistritoCsv distrito = deserializer.next();
 					Distrito d = new Distrito();
 					d.setId(distrito.getId());
 					d.setName(distrito.getName());
 					try {
 						addressService.createDistrito(d);
 					} catch (PreexistingEntityException e) {
 						// TODO try to edit?
 					}
 				}
 				deserializer.close(true);
 			}
 		} finally {
 			LineIterator.closeQuietly(lineItr);
 		}
 	}
 
 	/**
 	 * Handle concelho csv file.
 	 * 
 	 * @param is
 	 *            the is
 	 * @throws Exception 
 	 * @throws PreexistingEntityException 
 	 */
 	public void handleConcelhoCsvFile(InputStream is) throws Exception {
 		LineIterator lineItr = IOUtils.lineIterator(is, "UTF-8");
 		Deserializer deserializer = CsvIOFactory.createFactory(
 				ConcelhoCsv.class).createDeserializer();
 		try {
 			while (lineItr.hasNext()) {
 				String line = lineItr.nextLine();
 				StringReader reader = new StringReader(line);
 				deserializer.open(reader);
 				while (deserializer.hasNext()) {
 					ConcelhoCsv concelho = deserializer.next();
 					Concelho c = new Concelho();
 					ConcelhoPK pk = new ConcelhoPK();
 					pk.setIdDistrito(concelho.getIdDistrito());
 					pk.setIdConcelho(concelho.getIdConcelho());
 					c.setId(pk);
 					c.setName(concelho.getName());
 					try {
 						addressService.createConcelho(c);
 					} catch (PreexistingEntityException e) {
 						//TODO try to edit?
 					}
 				}
 				deserializer.close(true);
 			}
 		} finally {
 			LineIterator.closeQuietly(lineItr);
 		}
 	}
 
 	/**
 	 * Handle todos codigos postais csv file.
 	 * 
 	 * @param is
 	 *            the is
 	 * @throws Exception 
 	 * @throws PreexistingEntityException 
 	 */
 	public void handleTodosCodigosPostaisCsvFile(InputStream is)
 			throws Exception {
 		LineIterator lineItr = IOUtils.lineIterator(is, "UTF-8");
 		Deserializer deserializer = CsvIOFactory.createFactory(
 				TodosCodigosPostaisCsv.class).createDeserializer();
 		try {
 			while (lineItr.hasNext()) {
 				String line = lineItr.nextLine();
 				StringReader reader = new StringReader(line);
 				deserializer.open(reader);
 				while (deserializer.hasNext()) {
 					TodosCodigosPostaisCsv all = deserializer.next();
 					try {
 						handleLocalidade(all);
 					} catch (Exception e) {
 						// TODO  try to edit?
 					}
 					handleArteria(all);
 					handleCodigoPostal(all);
 				}
 				deserializer.close(true);
 			}
 		} finally {
 			LineIterator.closeQuietly(lineItr);
 		}
 	}
 
 	/**
 	 * Handle localidade.
 	 * 
 	 * @param all
 	 *            the all
 	 * @throws Exception 
 	 * @throws PreexistingEntityException 
 	 */
 	private void handleLocalidade(TodosCodigosPostaisCsv all) throws PreexistingEntityException, Exception {
 		Localidade l = new Localidade();
 		LocalidadePK pk = new LocalidadePK(all.getIdDistrito(), all.getIdConcelho(), all.getIdLocalidade());
 		l.setId(pk);
 		l.setName(all.getLocalidadeName());
 		addressService.createLocalidade(l);
 	}
 
 	/**
 	 * Handle arteria.
 	 * 
 	 * @param all
 	 *            the all
 	 */
 	private void handleArteria(TodosCodigosPostaisCsv all) {
 		Arteria a = new Arteria();
 		ArteriaPK pk = new ArteriaPK(all.getIdDistrito(), all.getIdConcelho(), all.getIdLocalidade(), all.getIdArteria());
 		a.setId(pk);
 		a.setArteriaName(all.getArteriaName());
 		a.setArteriaLocal(all.getArteriaLocal());
 		a.setArteriaTitle(all.getArteriaTitle());
 		a.setArteriaType(all.getArteriaType());
 		a.setCliente(all.getCliente());
 		a.setFirstPrep(all.getFirstPrep());
 		a.setPorta(all.getPorta());
 		a.setSecondPrep(all.getSecondPrep());
 		a.setTroco(all.getTroco());	
 		addressService.createArteria(a);
 	}
 
 	/**
 	 * Handle codigo postal.
 	 * 
 	 * @param all
 	 *            the all
 	 */
 	private void handleCodigoPostal(TodosCodigosPostaisCsv all) {
 
 	}
 }
