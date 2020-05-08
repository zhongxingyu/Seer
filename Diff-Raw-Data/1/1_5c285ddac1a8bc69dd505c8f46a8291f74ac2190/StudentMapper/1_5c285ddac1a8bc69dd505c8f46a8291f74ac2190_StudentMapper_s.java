 
 package pt.uac.cafeteria.model.persistence;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 import pt.uac.cafeteria.model.domain.Account;
 import pt.uac.cafeteria.model.domain.Student;
 import pt.uac.cafeteria.model.domain.Address;
 import pt.uac.cafeteria.model.domain.Course;
 import pt.uac.cafeteria.model.persistence.abstracts.DatabaseMapper;
 
 /**
  * Data Mapper for the Student domain object.
  */
 public class StudentMapper extends DatabaseMapper<Student> {
 
     /**
      * Creates a new StudentMapper instance.
      *
      * @param con a database connection object.
      */
     public StudentMapper(Connection con) {
         super(con);
     }
 
     @Override
     protected String table() {
         return "Alunos";
     }
 
     /**
      * Finds a list of Student domain objects from a name.
      * <p>
      * The name can be a partial match, and is not case sensitive.
      *
      * @param name a full or partial match for a student name.
      * @return A list of matching students.
      */
     public List<Student> findByName(String name) {
         String query = findStatement("a.nome RLIKE ?");
         return findMany(query, new String[]{name});
     }
 
     /**
      * SQL SELECT statement missing only the criteria to append to the
      * WHERE clause.
      *
      * @param criteria contents of "WHERE" part of the SQL statement.
      * @return A complete SQL statement string.
      */
     protected String findStatement(String criteria) {
         // We will attempt to load the foreign keys too, to save calls to the db.
         return "SELECT a.id, a.nome, telefone, email, bolsa, "
                 + "m.id AS idMorada, rua, nr, cod_postal, localidade, "
                 + "c.id AS idCurso, c.nome AS curso"
                 + " FROM " + table() + " AS a"
                 + " INNER JOIN Moradas AS m ON morada = m.id"
                 + " INNER JOIN Cursos  AS c ON a.curso = c.id"
                 + " WHERE " + criteria;
     }
 
     @Override
     protected String findStatement() {
         return findStatement("a.id = ?");
     }
 
     @Override
     protected Student doLoad(Integer id, ResultSet rs) throws SQLException {
         Student student;
 
         String name = rs.getString("nome");
 
         Integer addressId = new Integer(rs.getInt("idMorada"));
         Address address = loadAddress(addressId, rs);
 
         int phone = rs.getInt("telefone");
         String email = rs.getString("email");
         boolean scholarship = rs.getBoolean("bolsa");
 
         Integer courseId = new Integer(rs.getInt("idCurso"));
         Course course = loadCourse(courseId, rs);
 
         student = new Student(id, name, address, phone, email, scholarship, course);
         student.setAccount(loadAccount(id));
 
         return student;
     }
 
     /**
      * Loads an Address domain object from a result set.
      * <p>
      * Used for foreign key mapping, to save an additional call to the database.
      *
      * @param id the foreign key id.
      * @param rs the result set to load from.
      * @return An Address domain object.
      */
     protected Address loadAddress(Integer id, ResultSet rs) throws SQLException {
         AddressMapper addressMapper = MapperRegistry.address();
 
         if (addressMapper.isLoaded(id)) {
             return addressMapper.find(id);
         }
 
         String streetAddress = rs.getString("rua");
         String number = rs.getString("nr");
         String postalCode = rs.getString("cod_postal");
         String city = rs.getString("localidade");
 
         Address address = new Address(streetAddress, number, postalCode, city);
         address.setId(id);
 
         addressMapper.register(id, address);
         return address;
     }
 
     /**
      * Loads a Course domain object from a result set.
      * <p>
      * Used for foreign key mapping, to save an additional call to the database.
      *
      * @param id the foreign key id.
      * @param rs the result set to load from.
      * @return A Course domain object.
      */
     protected Course loadCourse(Integer id, ResultSet rs) throws SQLException {
         CourseMapper courseMapper = MapperRegistry.course();
 
         if (courseMapper.isLoaded(id)) {
             return courseMapper.find(id);
         }
 
         Course course = new Course(id, rs.getString("curso"));
         courseMapper.register(id, course);
         return course;
     }
 
     /**
      * Loads an Account domain object from the student id.
      *
      * @param id the student id number.
      * @return The corresponding Account object for the id.
      */
     protected Account loadAccount(Integer id) {
         Account account = MapperRegistry.account().find(id);
         if (account == null) {
             account = createNewAccount(id);
         }
         return account;
     }
 
     /**
      * Creates a new Account for a student.
      * <p>
      * Only needed when the account doesn't exist for an existing student,
      * for some reason.
      *
      * @param id the student id number.
      * @return The newly created Account object.
      */
     protected Account createNewAccount(Integer id) {
         return new Account(id);
     }
 
     @Override
     protected String insertStatement() {
         return "INSERT INTO " + table()
                 + "(id, nome, morada, telefone, email, bolsa, curso)"
                 + " VALUES"
                 + " (?, ?, ?, ?, ?, ?, ?)";
     }
 
     @Override
     protected void doInsert(Student student, PreparedStatement stmt) throws SQLException {
         Integer courseId = student.getCourse().getId();
         if (courseId == null) {
             courseId = MapperRegistry.course().insert(student.getCourse());
         }
         Integer addressId = student.getAddress().getId();
         if (addressId == null) {
             addressId = MapperRegistry.address().insert(student.getAddress());
         }
         stmt.setInt(1, student.getId());
         stmt.setString(2, student.getName());
         stmt.setInt(3, addressId);
         stmt.setString(4, String.valueOf(student.getPhone()));
         stmt.setString(5, student.getEmail());
         stmt.setBoolean(6, student.hasScholarship());
         stmt.setInt(7, courseId);
     }
 
     @Override
     public Integer insert(Student student) {
         Integer id = super.insert(student);
         Account newAccount = createNewAccount(id);
         MapperRegistry.account().insert(newAccount);
         student.setAccount(newAccount);
         return id;
     }
 
     @Override
     protected String updateStatement() {
         return "UPDATE " + table() + " SET "
                 + "nome = ?, morada = ?, telefone = ?, "
                 + "email = ?, bolsa = ?, curso = ? "
                 + "WHERE id = ?";
     }
 
     @Override
     protected void doUpdate(Student student, PreparedStatement stmt) throws SQLException {
         MapperRegistry.address().update(student.getAddress());
         MapperRegistry.course().update(student.getCourse());
 
         if (student.getAccount() != null) {
             MapperRegistry.account().update(student.getAccount());
         }
 
         stmt.setString(1, student.getName());
         stmt.setInt(2, student.getAddress().getId());
         stmt.setString(3, student.getPhone().toString());
         stmt.setString(4, student.getEmail());
         stmt.setInt(5, student.hasScholarship() ? 1 : 0 );
         stmt.setInt(6, student.getCourse().getId());
         stmt.setInt(7, student.getId());
     }
 }
