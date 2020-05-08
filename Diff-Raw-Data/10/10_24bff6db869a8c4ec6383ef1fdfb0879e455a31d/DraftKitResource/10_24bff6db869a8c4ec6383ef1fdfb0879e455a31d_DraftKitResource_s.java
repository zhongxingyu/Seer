 package de.kile.zapfmaster2000.rest.api.draftkit;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.jboss.resteasy.annotations.Suspend;
 import org.jboss.resteasy.spi.AsynchronousResponse;
 
 import de.kile.zapfmaster2000.rest.constants.PlatformConstants;
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Admin;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Box;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Zapfmaster2000Factory;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Zapfmaster2000Package;
 
 @Path("/draftkit")
 public class DraftKitResource {
 
 	private static final Logger LOG = Logger.getLogger(DraftKitResource.class);
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response retriveAvailableDraftKits(@QueryParam("token") String pToken) {
 
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 
 		if (account != null) {
 			List<DraftKitResponse> kits = retrieveDraftKits(account.getId());
 			return Response.ok(kits).build();
 		} else {
 			return Response.status(Status.FORBIDDEN).build();
 		}
 	}
 
 	@GET
 	@Path("/{draftKitId}/rfid")
 	@Produces(MediaType.APPLICATION_JSON)
 	public void retrieveUnknownRfidTags(
 			final @Suspend(PlatformConstants.ASYNC_TIMEOUT) AsynchronousResponse pResponse,
 			@QueryParam(value = "token") String pToken,
 			@PathParam("draftKitId") long pDraftKitId) {
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 		if (account != null) {
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			@SuppressWarnings("unchecked")
 			List<Box> result = session
 					.createQuery(
 							"From Box b WHERE b.account.id = :accountId AND b.id = :boxId")
 					.setLong("accountId", account.getId())
 					.setLong("boxId", pDraftKitId).list();
 			tx.commit();
 
 			if (result.size() == 1) {
 				Box box = result.get(0);
 				Zapfmaster2000Core.INSTANCE.getPushService()
 						.addUnkownRfidRequest(pResponse, box);
 			} else {
 				// TODO: error
 				LOG.warn("Draftkit " + pDraftKitId
 						+ " does not belong to account " + account.getId());
 			}
 
 		} else {
 			// TODO:
 			// pResponse.setResponse(Response.status(Status.FORBIDDEN).build());
 			LOG.warn("Forbidden");
 		}
 	}
 
 	@POST
 	@Path("/{draftKitId}/switchkeg")
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public Response switchKeg(@FormParam("token") String pToken,
 			@FormParam("size") int pSize, @FormParam("brand") String pBrand,
 			@PathParam("draftKitId") long pDraftKitId) {
 
 		LOG.info("Switching keg for box " + pDraftKitId + " to " + pBrand + "("
 				+ pSize + "l)");
 
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 		if (account != null) {
 			// check that the chosen box exists for given account
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			Box box = (Box) session.load(Zapfmaster2000Package.eINSTANCE
 					.getBox().getName(), pDraftKitId);
 
 			boolean canSwitch = box != null
 					&& box.getAccount().getId() == account.getId();
 			tx.commit();
 
 			if (canSwitch) {
 				Zapfmaster2000Core.INSTANCE.getKegService().switchKeg(box,
 						pBrand, pSize);
 				return Response.ok().build();
 			}
 
 		}
 
 		return Response.status(Status.FORBIDDEN).build();
 
 	}
 
 	@PUT
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public Response createBox(
 			@FormParam("accountId") long pAccountId,
 			@FormParam("version") @DefaultValue("1.0") String pVersion,
 			@FormParam("passphrase") String pPassphrase,
 			@FormParam("location") String pLocation,
 			@FormParam("a0") @DefaultValue("0.00006186472614225462") double pA0,
 			@FormParam("a1") @DefaultValue("0.0000825562566780224") double pA1,
 			@FormParam("a2") @DefaultValue("-1.675383403699784e-8") double pA2) {
 		LOG.info("Create box for account" + pAccountId + " with Passphrase"
 				+ pPassphrase + ",location: " + pLocation + " a0,a1,a2:" + pA0
 				+ "," + pA1 + "," + pA2);
 
 		// check that the chosen box exists for given account
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		Account account = (Account) session
 				.createQuery("From Account a WHERE a.id = :accountId")
 				.setLong("accountId", pAccountId).uniqueResult();
 
 		if (account != null && pPassphrase != null && !pPassphrase.equals("")
 				&& pLocation != null && !pLocation.equals("")) {
 			Box box = Zapfmaster2000Factory.eINSTANCE.createBox();
 			box.setVersion(pVersion);
 			box.setPassphrase(pPassphrase);
 			box.setAccount(account);
 
 			box.setLocation(pLocation);
 			box.setA0(pA0);
 			box.setA1(pA1);
 			box.setA2(pA2);
 
 			session.save(box);
 			tx.commit();
 			return Response.ok().build();
 
 		} else {
 			tx.commit();
 			return Response.status(Status.BAD_REQUEST).build();
 		}
 
 	}
 
 	@GET
 	@Path("/account/{accountId}")
 	public Response retrieveAccountBoxes(@PathParam("accountId") long accountId,
 			@QueryParam("token") String pToken) {
 
 		Admin admin = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAdmin(pToken);
 
 		if (admin != null) {
 			final List<DraftKitResponse> kits = retrieveDraftKits(accountId);
 			return Response.ok(kits).build();
 		} else {
 			return Response.status(Status.FORBIDDEN).build();
 		}
 
 	}
 
 	private List<DraftKitResponse> retrieveDraftKits(long accountId) {
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		@SuppressWarnings("unchecked")
 		List<Box> result = session
 				.createQuery("From Box b WHERE b.account.id = :accountId")
 				.setLong("accountId", accountId).list();
 		tx.commit();
 
 		List<DraftKitResponse> kits = new ArrayList<>();
 		for (Box box : result) {
 			DraftKitResponse response = new DraftKitResponse();
 			response.setBoxId(box.getId());
 			response.setName(box.getLocation());
 			response.setPassphrase(box.getPassphrase());
 			kits.add(response);
 		}
 		return kits;
 	}
 }
