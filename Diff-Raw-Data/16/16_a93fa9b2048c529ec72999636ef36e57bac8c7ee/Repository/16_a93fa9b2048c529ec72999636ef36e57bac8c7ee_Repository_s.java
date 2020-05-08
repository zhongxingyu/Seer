 /**
  * Written by Tien Nguyen <lilylnx@users.sf.net>
  * FREE FOR ALL BUT DOES NOT MEAN THERE IS NO PRICE.
  */
 package net.lilylnx.springnet.repository;
 
 import java.util.List;
 
 /**
  * Generic / Các thao tác chung để xử lý kho chứa (repository).
  * 
  * @author Tien Nguyen
  * @version $Id: Repository.java,v 1.0 2011/06/23 1:58:09 lilylnx Exp $
  */
 public interface Repository<T> {
 
   /**
    * Lấy thực thể của đối tượng dựa theo id.
    * @param id ID cần tìm kiếm
    * @return Thực thể được yêu cầu, hoặc <code>null</code> nếu không tìm thấy
    */
  T get(int id);
 
   /**
    * Thêm một thực thể mới của một đối tượng vào repository.
    * @param entity Thực cần lưu
    */
   void add(T entity);
 
   /**
    * Xóa một thực thể từ repository.
    * @param entity Thực thể cần xóa
    */
   void remove(T entity);
 
   /**
    * Cập nhật thông tin của một thực thể trong repository.
    * @param entity Thực thể cần cập nhật
    */
   void update(T entity);
   
   // Bổ sung thêm đồ chơi :D
   
   T find(Object id);
   T find(Object id, boolean locked);
   
   List<T> findAll();
   List<T> findAll(boolean cacheable);
   List<T> findAll(boolean reverse, String propertyName);
   List<T> findAll(boolean reverse, String propertyName, boolean cacheable);
   
   List<T> findRange(int[] range);
   List<T> findRange(int[] range, boolean reverse, String propertyName);
   
   List<T> select(String where);
   List<T> select(String[] orders);
   List<T> select(String where, String[] orders);
   List<T> select(String where, String[] orders, int[] range);
   
   Number count();
 
 }
