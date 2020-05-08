 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package aplikasi.peserta.rest.springmvc;
 
 import aplikasi.peserta.domain.Peserta;
 import aplikasi.peserta.service.ManajemenPesertaService;
 import aplikasi.peserta.service.impl.dummy.ManajemenPesertaServiceDummy;
 import java.util.ArrayList;
 import java.util.Date;
import java.util.HashMap;
 import java.util.List;
import java.util.Map;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  *
  * @author Student-03
  */
 @Controller
 @RequestMapping("/peserta")
 public class PesertaController {
     static ManajemenPesertaService service = new ManajemenPesertaServiceDummy();
     
     
     public PesertaController(){
         Peserta p = new Peserta();
         p.setNomerPeserta("ABC-123");
         p.setNama("Endy Muhardin");
         p.setTanggalLahir(new Date());
         service.simpan(p);
     }
     
     @RequestMapping(value="/", method=RequestMethod.GET)
     @ResponseBody
     public List<Peserta> semuaPeserta(@RequestParam(required=false) String nama){
         Integer start = 0; 
         Integer rows = 10;
         
         List<Peserta> semua = service.findSemuaPeserta(start, rows);
         List<Peserta> hasil = new ArrayList<Peserta>();
         
         // harusnya ini query ke db select * from peserta where nama like ?
         if(nama != null && !nama.isEmpty()){
             for (Peserta peserta : semua) {
                 if(peserta.getNama().contains(nama)){
                     hasil.add(peserta);
                 }
             }
         } else {
             hasil.addAll(semua);
         }
         
         return hasil;
     }
     
     @RequestMapping(value="/", method=RequestMethod.POST)
    @ResponseBody
    public Peserta simpan(@RequestBody Peserta peserta){
         service.simpan(peserta);
        return peserta;
     }
     
     @RequestMapping(value="/{id}", method=RequestMethod.GET)
     @ResponseBody
     public Peserta findPesertaById(@PathVariable Integer id){
         return service.findPesertaById(id);
     }
     
     @RequestMapping(value="/{id}", method=RequestMethod.PUT)
    @ResponseBody
    public Peserta update(@PathVariable Integer id, @RequestBody Peserta peserta){
         Peserta px = service.findPesertaById(id);
         px.setNama(peserta.getNama());
         px.setNomerPeserta(peserta.getNomerPeserta());
         px.setTanggalLahir(peserta.getTanggalLahir());
        return px;
     }
     
 }
