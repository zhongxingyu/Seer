 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package aplikasi.peserta.service;
 
 import aplikasi.peserta.domain.Peserta;
 import java.util.List;
 
 /**
  *
  * @author Student-03
  */
 public interface ManajemenPesertaService {
     public void simpan(Peserta p );
     public Peserta findPesertaById(Integer id) ;
    public Peserta findPesertaByNomerPeserta(String no);
     public List<Peserta> findSemuaPeserta(Integer start, Integer rows);
     public Long countSemuaPeserta();
 }
