 package com.jayway.demo.library.rest.resources.hateoas;
 
 import java.util.Collection;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 
 import com.jayway.demo.library.domain.BookRepository;
 import com.jayway.demo.library.domain.Customer;
 import com.jayway.demo.library.domain.CustomerRepository;
 import com.jayway.demo.library.domain.factory.RepositoryFactory;
 import com.jayway.demo.library.rest.dto.CustomerDto;
 import com.jayway.demo.library.rest.dto.LoanDto;
 import com.jayway.jaxrs.hateoas.Linkable;
 import com.jayway.jaxrs.hateoas.core.HateoasResponse;
 
 @Path("/library/customers")
 public class CustomerResource {
 
 	private CustomerRepository customerRepository;
 	private BookRepository bookRepository;
 
 	public CustomerResource() {
 		customerRepository = RepositoryFactory.getCustomerRepository();
 		bookRepository = RepositoryFactory.getBookRepository();
 	}
 
 	@GET
 	@Produces("application/vnd.demo.library.list.customer+json")
	@Linkable(id = LinkableIds.CUSTOMER_LIST_ID, rel = Rels.CUSTOMERS_REL)
 	public Response getAllCustomers() {
 		return HateoasResponse
 				.ok(CustomerDto.fromBeanCollection(customerRepository
 						.getAllCustomers())).link(LinkableIds.CUSTOMER_NEW_ID)
 				.each(LinkableIds.CUSTOMER_DETAILS_ID, "id").build();
 	}
 
 	@POST
 	@Consumes("application/vnd.demo.library.customer+json")
 	@Produces("application/vnd.demo.library.customer+json")
	@Linkable(id = LinkableIds.CUSTOMER_NEW_ID, rel = Rels.CUSTOMERS_REL, templateClass = CustomerDto.class)
 	public Response newCustomer(CustomerDto customer) {
 		Customer newCustomer = customerRepository.newCustomer(customer
 				.getName());
 		return HateoasResponse
 				.created(LinkableIds.CUSTOMER_DETAILS_ID, newCustomer.getId())
 				.selfLink(LinkableIds.CUSTOMER_DETAILS_ID, newCustomer.getId())
 				.entity(CustomerDto.fromBean(newCustomer)).build();
 	}
 
 	@GET
 	@Path("/{id}")
 	@Produces("application/vnd.demo.library.customer+json")
 	@Linkable(id = LinkableIds.CUSTOMER_DETAILS_ID, rel = Rels.CUSTOMER_REL)
 	public Response getCustomer(@PathParam("id") Integer id) {
 		return HateoasResponse
 				.ok(CustomerDto.fromBean(customerRepository.getById(id)))
 				.link(LinkableIds.CUSTOMER_LOANS_ID, id).build();
 	}
 
 	@GET
 	@Path("/{id}/loans")
 	@Produces("application/vnd.demo.library.list.loan+json")
	@Linkable(id = LinkableIds.CUSTOMER_LOANS_ID, rel = Rels.LOANS_REL)
 	public Response getCustomerLoans(@PathParam("id") Integer id) {
 
 		Collection<LoanDto> loanDtos = LoanDto
 				.fromBeanCollection(bookRepository.getLoansForCustomer(id));
 		return HateoasResponse.ok(loanDtos)
 				.each(LinkableIds.LOAN_DETAILS_ID, "bookId").build();
 	}
 
 }
