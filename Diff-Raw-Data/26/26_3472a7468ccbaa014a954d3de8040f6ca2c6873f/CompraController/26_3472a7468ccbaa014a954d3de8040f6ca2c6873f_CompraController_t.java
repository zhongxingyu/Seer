 package web.ecommerce.tpfinal.ecommerce_web.controller;
 
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import web.ecommerce.tpfinal.ecommerce_web.account.Account;
 import web.ecommerce.tpfinal.ecommerce_web.account.AccountRepository;
 import web.ecommerce.tpfinal.ecommerce_web.clasesDominio.Compra;
 import web.ecommerce.tpfinal.ecommerce_web.clasesDominio.Producto;
 import web.ecommerce.tpfinal.ecommerce_web.clasesDominio.ProductoComprable;
 import web.ecommerce.tpfinal.ecommerce_web.repository.CompraRepository;
 import web.ecommerce.tpfinal.ecommerce_web.repository.OrdenDeCompraRepository;
 import web.ecommerce.tpfinal.ecommerce_web.repository.ProductoComprableRepository;
 import web.ecommerce.tpfinal.ecommerce_web.repository.ProductoRepository;
 
 @Controller
 @RequestMapping(value="/commerce/**")
 @SessionAttributes("carrito")
 public class CompraController {
 	
 		@Autowired
 		private OrdenDeCompraRepository ordenDeCompraRepository;
 
 		@Autowired
 		private ProductoComprableRepository productoComprableRepository;
 
 		@Autowired
 		private CompraRepository compraRepository;
 
 		@Autowired
 		private ProductoRepository productoRepository;
 
 		@Autowired
 		private AccountRepository accountRepository;
 
 		@RequestMapping(value="/carrito", method=RequestMethod.GET)
 		public ModelAndView carrito(Principal principal, HttpServletRequest request){
 			ModelAndView mav ;
 			@SuppressWarnings("unchecked")
 			ArrayList<ProductoComprable> carrito = (ArrayList<ProductoComprable>) request.getSession().getAttribute("carrito");
 			if(carrito == null || carrito.isEmpty()){
 				mav = new ModelAndView("redirect:error");
 				String email = principal.getName();
 				mav.getModelMap().addAttribute("email", email);
 			}else{
 				mav = new ModelAndView(); 
 				mav.getModelMap().addAttribute("carrito", carrito);
 				String email = principal.getName();
 				mav.getModelMap().addAttribute("email", email);
 			}
 			return mav;
 		}
 
 		@RequestMapping(value="/error", method=RequestMethod.GET)
 		public ModelAndView error(Principal principal){
 			ModelAndView mav = new ModelAndView();
 			String email = principal.getName();
 			mav.getModelMap().addAttribute("email", email);
 			mav.getModelMap().addAttribute("mensaje", "El carrito de Compras está vacío. Realice una búsqueda y agregue productos");	
 			return mav;
 		}
 
 		@RequestMapping(value="/borrar", method=RequestMethod.GET)
 		public ModelAndView borrar(@RequestParam long id, HttpServletRequest request){
 			ModelAndView mav = new ModelAndView("redirect:carrito");	
 			@SuppressWarnings("unchecked")
 			ArrayList<ProductoComprable> carrito = (ArrayList<ProductoComprable>) request.getSession().getAttribute("carrito");
 			for(int i=0; i<carrito.size(); ){
 				if(carrito.get(i).getId()== id){
 					carrito.remove(i);
 				}else{
 					i++;
 				}
 			}
 			request.getSession().setAttribute("carrito", carrito);
 			return mav;
 		}
 
 		@RequestMapping(value="/guardar", method=RequestMethod.GET)
 		public ModelAndView guardar(@RequestParam long id, HttpServletRequest request, @RequestParam int cantidad){
 			ModelAndView mav = new ModelAndView("redirect:carrito");	
 			@SuppressWarnings("unchecked")
 			ArrayList<ProductoComprable> carrito = (ArrayList<ProductoComprable>) request.getSession().getAttribute("carrito");
 			ProductoComprable productoComprable = productoComprableRepository.get(id);
 			ProductoComprable productoComprableNuevo = productoComprable;
 			productoComprableNuevo.setCantidad(cantidad);
 			for(int i=0; i<carrito.size(); ){
 				if(carrito.get(i).getId()== id){
 					carrito.remove(i);
 				}else{
 					i++;
 				}
 			}
 			carrito.add(productoComprableNuevo);
 			request.getSession().setAttribute("carrito", carrito);
 			return mav;
 		}
 
 		@RequestMapping(value="/detalle", method=RequestMethod.POST)
 		public ModelAndView detalle(@RequestParam long id){
 			ModelAndView mav = new ModelAndView();
 			Producto producto = productoRepository.getP(id);
 			mav.getModelMap().addAttribute(producto);
 			return mav;
 		}
 		
 		@RequestMapping(value="/volverAlCarrito", method=RequestMethod.POST)
 		public ModelAndView volverAlCarrito(){
 			ModelAndView mav = new ModelAndView("redirect:carrito");
 			return mav;
 		}
 		
 		@RequestMapping(value="crearChanguito"/*accion*/, method=RequestMethod.POST)
 		public ModelAndView crearChanguito(HttpServletRequest request, Principal principal){
 			ModelAndView mav = new ModelAndView("redirect:carrito");
 			Account account= null;
 			//Obtengo el carrito
 			ArrayList<ProductoComprable> carrito = (ArrayList<ProductoComprable>) request.getSession().getAttribute("carrito");
 			//Obtengo la cuenta
 			String email = principal.getName();
 			List<Account> accounts = accountRepository.getAll();
 			for(int i=0; i<accounts.size();){
 				if(accounts.get(i).getEmail().equals(email)){
 					account = accounts.get(i);
 					i++;
 				}else i++;
 			} 
 			Compra compra = new Compra(account, carrito);
			ordenDeCompraRepository.cargarChanguitoDB(compra, carrito);//llama a ordenDeCompraRepository.cargarChanguitoDB() para cargar un objeto
 			return mav;
 		}
 }
