 package cl.votainteligente.legislativo.controllers.debate;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import cl.votainteligente.legislativo.ApplicationProperties;
 import cl.votainteligente.legislativo.ServiceException;
 import cl.votainteligente.legislativo.common.Page;
 import cl.votainteligente.legislativo.exceptions.BadRequestException;
 import cl.votainteligente.legislativo.exceptions.ServerErrorException;
 import cl.votainteligente.legislativo.model.Bill;
 import cl.votainteligente.legislativo.model.Debate;
 import cl.votainteligente.legislativo.model.domainobjects.DebateDO;
 import cl.votainteligente.legislativo.service.bill.BillService;
 import cl.votainteligente.legislativo.service.debate.DebateService;
 
 @Controller
 public class DebateController {
 	private SimpleDateFormat dateFormat = new SimpleDateFormat(
 			ApplicationProperties.getProperty("controller.date.format"));
 
 	@Autowired
 	DebateService debateService;
 
 	@Autowired
 	BillService billService;
 
 	@RequestMapping(params = { "id" }, value = "debate/any", method = RequestMethod.GET)
 	@ResponseBody
	public final Debate getBillById(
 			@RequestParam(value = "id", required = true) final long id) {
 		try {
 			return debateService.getDebate(id);
 		} catch (ServiceException e) {
 			e.printStackTrace();
 			throw new ServerErrorException();
 		}
 	}
 
 	@RequestMapping(value = "debate/dateRange", method = RequestMethod.GET)
 	@ResponseBody
 	public final Page<DebateDO> getDateRange(
 			@RequestParam(value = "from", required = true) final String fromString,
 			@RequestParam(value = "to", required = true) final String toString,
 			@RequestParam(value = "page", defaultValue = ApplicationProperties.CONTROLLER_PAGE_DEFAULT_VALUE, required = false) final int page,
 			@RequestParam(value = "perPage", defaultValue = ApplicationProperties.CONTROLLER_PER_PAGE_DEFAULT_VALUE, required = false) final int perPage) {
 		try {
 			Date from = dateFormat.parse(fromString);
 			Date to = dateFormat.parse(toString);
 			Page<DebateDO> resultPage = debateService.getDebateByDateRange(
 					from, to, page, perPage);
 			return resultPage;
 		} catch (ServiceException e) {
 			e.printStackTrace();
 			throw new ServerErrorException();
 		} catch (ParseException e) {
 			throw new BadRequestException();
 		}
 	}
 
 	@RequestMapping(params = { "id" }, value = "debate/bill", method = RequestMethod.GET)
 	@ResponseBody
 	public final Page<DebateDO> getDebateByBill(
 			@RequestParam(value = "id", required = true) final long id,
 			@RequestParam(value = "page", defaultValue = ApplicationProperties.CONTROLLER_PAGE_DEFAULT_VALUE, required = false) final int page,
 			@RequestParam(value = "perPage", defaultValue = ApplicationProperties.CONTROLLER_PER_PAGE_DEFAULT_VALUE, required = false) final int perPage) {
 		try {
 			Bill bill = billService.getBill(id);
 			return debateService.getDebateByBill(bill, page, perPage);
 		} catch (ServiceException e) {
 			e.printStackTrace();
 			throw new ServerErrorException();
 		}
 	}
 }
