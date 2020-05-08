 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package edu.unidata.ucar.netcdf.jna;
 
 import com.sun.jna.Native;
 import com.sun.jna.NativeLong;
 import com.sun.jna.Pointer;
 import com.sun.jna.ptr.ByteByReference;
 import com.sun.jna.ptr.DoubleByReference;
 import com.sun.jna.ptr.FloatByReference;
 import com.sun.jna.ptr.IntByReference;
 import com.sun.jna.ptr.NativeLongByReference;
 import com.sun.jna.ptr.ShortByReference;
 import java.nio.Buffer;
 import java.nio.ByteBuffer;
 import java.nio.DoubleBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.nio.LongBuffer;
 import java.nio.ShortBuffer;
 
 /**
  *
  * @author tkunicki
  * 
  */
 public class NC  {
 
     static {
         Native.register("netcdf");
     }
 
 //typedef unsigned int size_t;
 //typedef int ptrdiff_t;
 //typedef int nc_type;
 //#define NO_NETCDF_2
 
 	public final static int	NC_NAT      = 0;        /* NAT = 'Not A Type' (c.f. NaN) */
 	public final static int	NC_BYTE     = 1;        /* signed 1 byte integer */
 	public final static int	NC_CHAR 	= 2;        /* ISO/ASCII character */
 	public final static int	NC_SHORT    = 3;        /* signed 2 byte integer */
 	public final static int	NC_INT      = 4;        /* signed 4 byte integer */
 	public final static int NC_LONG     = NC_INT;   /* deprecated, but required for backward compatibility. */
 	public final static int	NC_FLOAT    = 5;        /* single precision floating point number */
 	public final static int	NC_DOUBLE   = 6;        /* double precision floating point number */
 	public final static int	NC_UBYTE    = 7;        /* unsigned 1 byte int */
 	public final static int	NC_USHORT   = 8;        /* unsigned 2-byte int */
 	public final static int	NC_UINT     = 9;        /* unsigned 4-byte int */
 	public final static int	NC_INT64    = 10;       /* signed 8-byte int */
 	public final static int	NC_UINT64   = 11;       /* unsigned 8-byte int */
 	public final static int	NC_STRING   = 12;       /* string */
 
 /* The following are use internally in support of user-defines
  * types. They are also the class returned by nc_inq_user_type. */
 	public final static int	NC_VLEN     = 13;       /* used internally for vlen types */
 	public final static int	NC_OPAQUE   = 14;       /* used internally for opaque types */
 	public final static int	NC_ENUM     = 15;       /* used internally for enum types */
 	public final static int	NC_COMPOUND = 16;       /* used internally for compound types */
 
 /*
  * 	Default fill values, used unless _FillValue attribute is set.
  * These values are stuffed into newly allocated space as appropriate.
  * The hope is that one might use these to notice that a particular datum
  * has not been set.
  */
 //	public final static int NC_FILL_BYTE	((signed char)-127)
 //	public final static int NC_FILL_BYTE	((signed char)-127)
 //	public final static int NC_FILL_CHAR	((char)0)
 //	public final static int NC_FILL_SHORT	((short)-32767)
 //	public final static int NC_FILL_INT	(-2147483647L)
 //	public final static int NC_FILL_FLOAT	(9.9692099683868690e+36f) /* near 15 * 2^119 */
 //	public final static int NC_FILL_DOUBLE	(9.9692099683868690e+36)
 //	public final static int NC_FILL_UBYTE   (255)
 //	public final static int NC_FILL_USHORT  (65535)
 //	public final static int NC_FILL_UINT    (4294967295U)
 //	public final static int NC_FILL_INT64   ((long long)-9223372036854775806LL)
 //	public final static int NC_FILL_UINT64  ((unsigned long long)18446744073709551614ULL)
 //	public final static int NC_FILL_STRING  ""
 
 /* These represent the max and min values that can be stored in a
  * netCDF file for their associated types. Recall that a C compiler
  * may define int to be any length it wants, but a NC_INT is *always*
  * a 4 byte signed int. On a platform with has 64 bit ints, there will
  * be many ints which are outside the range supported by NC_INT. But
  * since NC_INT is an external format, it has to mean the same thing
  * everywhere. */
 //	public final static int NC_MAX_BYTE 127
 //	public final static int NC_MIN_BYTE (-NC_MAX_BYTE-1)
 //	public final static int NC_MAX_CHAR 255
 //	public final static int NC_MAX_SHORT 32767
 //	public final static int NC_MIN_SHORT (-NC_MAX_SHORT - 1)
 //	public final static int NC_MAX_INT 2147483647
 //	public final static int NC_MIN_INT (-NC_MAX_INT - 1)
 //	public final static int NC_MAX_FLOAT 3.402823466e+38f
 //	public final static int NC_MIN_FLOAT (-NC_MAX_FLOAT)
 //	public final static int NC_MAX_DOUBLE 1.7976931348623157e+308
 //	public final static int NC_MIN_DOUBLE (-NC_MAX_DOUBLE)
 //	public final static int NC_MAX_UBYTE NC_MAX_CHAR
 //	public final static int NC_MAX_USHORT 65535U
 //	public final static int NC_MAX_UINT 4294967295U
 //	public final static int NC_MAX_INT64 (9223372036854775807LL)
 //	public final static int NC_MIN_INT64 (-9223372036854775807LL-1)
 //	public final static int NC_MAX_UINT64 (18446744073709551615ULL)
 //	public final static int X_INT64_MAX     (9223372036854775807LL)
 //	public final static int X_INT64_MIN     (-X_INT64_MAX - 1)
 //	public final static int X_UINT64_MAX    (18446744073709551615ULL)
 
 /*
  * The above values are defaults.  If you wish a variable to use a
  * different value than the above defaults, create an attribute with
  * the same type as the variable and the following reserved name. The
  * value you give the attribute will be used as the fill value for
  * that variable.
  */
 	public final static String _FillValue   = "_FillValue";
 	public final static int NC_FILL         = 0;        /* argument to ncsetfill to clear NC_NOFILL */
 	public final static int NC_NOFILL       = 0x100;    /* Don't fill data section an records */
 
 /*
  * Use these 'mode' flags for nc_open.
  */
 	public final static int NC_NOWRITE  = 0;        /* default is read only */
 	public final static int NC_WRITE    = 0x0001;   /* read & write */
 /*
  * Use these 'mode' flags for nc_create.
  */
 	public final static int NC_CLOBBER          = 0;
 	public final static int NC_NOCLOBBER        = 0x0004;   /* Don't destroy existing file on create */
 	public final static int NC_64BIT_OFFSET     = 0x0200;   /* Use large (64-bit) file offsets */
 	public final static int NC_NETCDF4          = 0x1000;   /* Use netCDF-4/HDF5 format */
 	public final static int NC_CLASSIC_MODEL    = 0x0100;   /* Enforce classic model when used with NC_NETCDF4. */
 /*
  * Use these 'mode' flags for both nc_create and nc_open.
  */
 	public final static int NC_SHARE            = 0x0800;   /* Share updates, limit cacheing */
 	public final static int NC_MPIIO            = 0x2000;
 	public final static int NC_MPIPOSIX         = 0x4000;
 	public final static int NC_PNETCDF          = 0x8000;
 /* The following flag currently is ignored, but use in
  * nc_open() or nc_create() may someday support use of advisory
  * locking to prevent multiple writers from clobbering a file
  */
 	public final static int NC_LOCK             = 0x0400;   /* Use locking if available */
 
 /*
  * Starting with version 3.6, there are different format netCDF
  * files. 4.0 introduces the third one. These defines are only for
  * the nc_set_default_format function.
  */
 	public final static int NC_FORMAT_CLASSIC           = (1);
 	public final static int NC_FORMAT_64BIT             = (2);
 	public final static int NC_FORMAT_NETCDF4           = (3);
 	public final static int NC_FORMAT_NETCDF4_CLASSIC   = (4); /* create netcdf-4 files, with NC_CLASSIC_MODEL. */
 
 /*
  * Let nc__create() or nc__open() figure out
  * as suitable chunk size.
  */
 	public final static int NC_SIZEHINT_DEFAULT = 0;
 
 /*
  * In nc__enddef(), align to the chunk size.
  */
 //#define NC_ALIGN_CHUNK ((size_t)(-1))
 
 /*
  * 'size' argument to ncdimdef for an unlimited dimension
  */
 	public final static long NC_UNLIMITED = 0L;
 
 /*
  * attribute id to put/get a global attribute
  */
 	public final static int NC_GLOBAL = -1;
 
 /*
  * These maximums are enforced by the interface, to facilitate writing
  * applications and utilities.  However, nothing is statically allocated to
  * these sizes internally.
  */
 	public final static int NC_MAX_DIMS     = 1024;         /* max dimensions per file */
 	public final static int NC_MAX_ATTRS    = 8192;         /* max global or per variable attributes */
 	public final static int NC_MAX_VARS     = 8192;         /* max variables per file */
 	public final static int NC_MAX_NAME     = 256;          /* max length of a name */
 	public final static int NC_MAX_VAR_DIMS = NC_MAX_DIMS;  /* max per variable dimensions */
 
 /* In HDF5 files you can set the endianness of variables with
  * nc_def_var_endian(). These defines are used there. */
 	public final static int NC_ENDIAN_NATIVE    = 0;
 	public final static int NC_ENDIAN_LITTLE    = 1;
 	public final static int NC_ENDIAN_BIG       = 2;
 
 /* In HDF5 files you can set storage for each variable to be either
  * contiguous or chunked, with nc_def_var_chunking().  These defines
  * are used there. */
 	public final static int NC_CHUNKED      = 0;
 	public final static int NC_CONTIGUOUS   = 1;
 
 /* In HDF5 files you can set check-summing for each variable.
  * Currently the only checksum available is Fletcher-32, which can be
  * set with the function nc_def_var_fletcher32.  These defines are used
  * there. */
 	public final static int NC_NOCHECKSUM   = 0;
 	public final static int NC_FLETCHER32   = 1;
 
 /* In HDF5 files you can specify that a shuffle filter should be used
  * on each chunk of a variable to improve compression for that
  * variable.  This per-variable shuffle property can be set with the
  * function nc_def_var_deflate.  These defines are used there. */
 	public final static int NC_NOSHUFFLE    = 0;
 	public final static int NC_SHUFFLE      = 1;
 
 /*
  * The netcdf version 3 functions all return integer error status.
  * These are the possible values, in addition to certain
  * values from the system errno.h.
  */
 
 //#define NC_ISSYSERR(err)	((err) > 0)
 
     public final static int NC_NOERR            = 0;	/* No Error */
 
 	public final static int NC2_ERR             = (-1);     /* Returned for all errors in the v2 API. */
 	public final static int	NC_EBADID           = (-33);    /* Not a netcdf id */
 	public final static int	NC_ENFILE           = (-34);    /* Too many netcdfs open */
 	public final static int	NC_EEXIST           = (-35);    /* netcdf file exists && NC_NOCLOBBER */
 	public final static int	NC_EINVAL           = (-36);    /* Invalid Argument */
 	public final static int	NC_EPERM            = (-37);    /* Write to read only */
 	public final static int NC_ENOTINDEFINE     = (-38);    /* Operation not allowed in data mode */
 	public final static int	NC_EINDEFINE        = (-39);    /* Operation not allowed in define mode */
 	public final static int	NC_EINVALCOORDS     = (-40);    /* Index exceeds dimension bound */
 	public final static int	NC_EMAXDIMS         = (-41);    /* NC_MAX_DIMS exceeded */
 	public final static int	NC_ENAMEINUSE       = (-42);    /* String match to name in use */
 	public final static int NC_ENOTATT          = (-43);    /* Attribute not found */
 	public final static int	NC_EMAXATTS         = (-44);    /* NC_MAX_ATTRS exceeded */
 	public final static int NC_EBADTYPE         = (-45);    /* Not a netcdf data type */
 	public final static int NC_EBADDIM          = (-46);    /* Invalid dimension id or name */
 	public final static int NC_EUNLIMPOS        = (-47);    /* NC_UNLIMITED in the wrong index */
 	public final static int	NC_EMAXVARS         = (-48);    /* NC_MAX_VARS exceeded */
 	public final static int NC_ENOTVAR          = (-49);    /* Variable not found */
 	public final static int NC_EGLOBAL          = (-50);    /* Action prohibited on NC_GLOBAL varid */
 	public final static int NC_ENOTNC           = (-51);    /* Not a netcdf file */
 	public final static int NC_ESTS             = (-52);    /* In Fortran, string too short */
 	public final static int NC_EMAXNAME         = (-53);    /* NC_MAX_NAME exceeded */
 	public final static int NC_EUNLIMIT         = (-54);    /* NC_UNLIMITED size already in use */
 	public final static int NC_ENORECVARS       = (-55);    /* nc_rec op when there are no record vars */
 	public final static int NC_ECHAR            = (-56);    /* Attempt to convert between text & numbers */
 	public final static int NC_EEDGE            = (-57);    /* Start+count exceeds dimension bound */
 	public final static int NC_ESTRIDE          = (-58);    /* Illegal stride */
 	public final static int NC_EBADNAME         = (-59);    /* Attribute or variable name
                                                            contains illegal characters */
 /* N.B. following must match value in ncx.h */
 	public final static int NC_ERANGE           = (-60);    /* Math result not representable */
 	public final static int NC_ENOMEM           = (-61);    /* Memory allocation (malloc) failure */
 
 	public final static int NC_EVARSIZE         = (-62);    /* One or more variable sizes violate
                                                            format constraints */
 	public final static int NC_EDIMSIZE         = (-63);    /* Invalid dimension size */
 	public final static int NC_ETRUNC           = (-64);    /* File likely truncated or possibly corrupted */
 
 	public final static int NC_EAXISTYPE        = (-65);    /* Unknown axis type. */
 
 /* Following errors are added for DAP */
 	public final static int NC_EDAP             = (-66);    /* Generic DAP error */
 	public final static int NC_ECURL            = (-67);    /* Generic libcurl error */
 	public final static int NC_EIO              = (-68);    /* Generic IO error */
 	public final static int NC_ENODATA          = (-69);    /* Attempt to access variable with no data */
 	public final static int NC_EDAPSVC          = (-70);    /* DAP Server side error */
 	public final static int NC_EDAS             = (-71);    /* Malformed or inaccessible DAS */
 	public final static int NC_EDDS             = (-72);    /* Malformed or inaccessible DDS */
 	public final static int NC_EDATADDS         = (-73);    /* Malformed or inaccessible DATADDS */
 	public final static int NC_EDAPURL          = (-74);    /* Malformed DAP URL */
 	public final static int NC_EDAPCONSTRAINT   = (-75);    /* Malformed DAP Constraint*/
 
 /* The following was added in support of netcdf-4. Make all netcdf-4
    error codes < -100 so that errors can be added to netcdf-3 if
    needed. */
 	public final static int NC4_FIRST_ERROR     = (-100);
 	public final static int NC_EHDFERR          = (-101);   /* Error at HDF5 layer. */
 	public final static int NC_ECANTREAD        = (-102);   /* Can't read. */
 	public final static int NC_ECANTWRITE       = (-103);   /* Can't write. */
 	public final static int NC_ECANTCREATE      = (-104);   /* Can't create. */
 	public final static int NC_EFILEMETA        = (-105);   /* Problem with file metadata. */
 	public final static int NC_EDIMMETA         = (-106);   /* Problem with dimension metadata. */
 	public final static int NC_EATTMETA         = (-107);   /* Problem with attribute metadata. */
 	public final static int NC_EVARMETA         = (-108);   /* Problem with variable metadata. */
 	public final static int NC_ENOCOMPOUND      = (-109);   /* Not a compound type. */
 	public final static int NC_EATTEXISTS       = (-110);   /* Attribute already exists. */
 	public final static int NC_ENOTNC4          = (-111);   /* Attempting netcdf-4 operation on netcdf-3 file. */
 	public final static int NC_ESTRICTNC3       = (-112);   /* Attempting netcdf-4 operation on strict nc3 netcdf-4 file. */
 	public final static int NC_ENOTNC3          = (-113);   /* Attempting netcdf-3 operation on netcdf-4 file. */
 	public final static int NC_ENOPAR           = (-114);   /* Parallel operation on file opened for non-parallel access. */
 	public final static int NC_EPARINIT         = (-115);   /* Error initializing for parallel access. */
 	public final static int NC_EBADGRPID        = (-116);   /* Bad group ID. */
 	public final static int NC_EBADTYPID        = (-117);   /* Bad type ID. */
 	public final static int NC_ETYPDEFINED      = (-118);   /* Type has already been defined and may not be edited. */
 	public final static int NC_EBADFIELD        = (-119);   /* Bad field ID. */
 	public final static int NC_EBADCLASS        = (-120);   /* Bad class. */
 	public final static int NC_EMAPTYPE         = (-121);   /* Mapped access for atomic types only. */
 	public final static int NC_ELATEFILL        = (-122);   /* Attempt to define fill value when data already exists. */
 	public final static int NC_ELATEDEF         = (-123);   /* Attempt to define var properties, like deflate, after enddef. */
 	public final static int NC_EDIMSCALE        = (-124);   /* Probem with HDF5 dimscales. */
 	public final static int NC_ENOGRP           = (-125);   /* No group found. */
 	public final static int NC_ESTORAGE         = (-126);   /* Can't specify both contiguous and chunking. */
 	public final static int NC_EBADCHUNK        = (-127);   /* Bad chunksize. */
 	public final static int NC_ENOTBUILT        = (-128);   /* Attempt to use feature that was not turned on when netCDF was built. */
 	public final static int NC4_LAST_ERROR      = (-128);
 
 /* This is used in netCDF-4 files for dimensions without coordinate
  * vars. */
     public final static String DIM_WITHOUT_VARIABLE = "This is a netCDF dimension but not a netCDF variable.";
 
 /* This is here at the request of the NCO team to support the stupid
  * mistake of having chunksizes be first ints, then size_t. Doh! */
 //#define NC_HAVE_NEW_CHUNKING_API 1
 
 /*
  * The Interface
  */
 
 
 //const char*
 //nc_inq_libvers(void);
     public static native String nc_inq_libvers();
 
 //const char*
 //nc_strerror(int ncerr);
     public static native String nc_strerror(int ncerror);
 
 //int
 //nc__create(const char *path, int cmode, size_t initialsz,
 //        size_t *chunksizehintp, int *ncidp);
     public static native int nc__create(String path, int cmode,
             NativeLong initialsz, IntByReference chunksizehitp, IntByReference ncidp);
 
 //int
 //nc_create(const char *path, int cmode, int *ncidp);
     public static native int nc_create(String path, int cmode, IntByReference ncidp);
 
 //int
 //nc__open(const char *path, int mode, size_t *chunksizehintp, int *ncidp);
     public static native int nc__open(String path, int cmode,
             NativeLongByReference chunksizehitp, IntByReference ncidp);
 
 //int
 //nc_open(const char *path, int mode, int *ncidp);
     public static native int nc_open(String path, int cmode, IntByReference ncidp);
 
 //int
 //nc_create_par(const char *path, int cmode, MPI_Comm comm, MPI_Info info,
 //        int *ncidp);
 
 //int
 //nc_open_par(const char *path, int mode, MPI_Comm comm, MPI_Info info,
 //        int *ncidp);
 
 /* Use these with nc_var_par_access(). */
     public final static int NC_INDEPENDENT  = 0;
     public final static int NC_COLLECTIVE   = 1;
 
 //int
 //nc_var_par_access(int ncid, int varid, int par_access);
 
 /* Given an ncid and group name (NULL gets root group), return
  * locid. */
 //int
 //nc_inq_ncid(int ncid, const char *name, int *grp_ncid);
 
 /* Given a location id, return the number of groups it contains, and
  * an array of their locids. */
 //int
 //nc_inq_grps(int ncid, int *numgrps, int *ncids);
 
 /* Given locid, find name of group. (Root group is named "/".) */
 //int
 //nc_inq_grpname(int ncid, char *name);
 
 /* Given ncid, find full name and len of full name. (Root group is
  * named "/", with length 1.) */
 //int
 //nc_inq_grpname_full(int ncid, size_t *lenp, char *full_name);
 
 /* Given ncid, find len of full name. */
 //int
 //nc_inq_grpname_len(int ncid, size_t *lenp);
 
 /* Given an ncid, find the ncid of its parent group. */
 //int
 //nc_inq_grp_parent(int ncid, int *parent_ncid);
 
 /* Given a name and parent ncid, find group ncid. */
 //int
 //nc_inq_grp_ncid(int ncid, const char *grp_name, int *grp_ncid);
 
 /* Given a full name and ncid, find group ncid. */
 //int
 //nc_inq_grp_full_ncid(int ncid, const char *full_name, int *grp_ncid);
 
 /* Get a list of ids for all the variables in a group. */
 //int nc_inq_varids(int ncid, int *nvars, int *varids);
     public static native int nc_inq_varids(int ncid, IntByReference nvars, int[] varids);
     
     public static int nc_inq_varids(int ncid, int[] varids) {
     	return nc_inq_varids(ncid, null, varids);
     }
     public static  int nc_inq_var_count(int ncid, IntByReference nvars) {
     	return nc_inq_varids(ncid, nvars, null);
     }
    
 
 
 /* Find all dimids for a location. This finds all dimensions in a
  * group, or any of its parents. */
 //int
 //nc_inq_dimids(int ncid, int *ndims, int *dimids, int include_parents);
     public static native int nc_inq_dimids(int ncid, IntByReference ndims, int[] dimids, int include_parents);
     
     public static int nc_inq_dimids(int ncid, int[] dimids) {
     	return nc_inq_dimids(ncid, null, dimids, 1);
     }
     public static int nc_inq_dim_count(int ncid, IntByReference ndims) {
     	return nc_inq_dimids(ncid, ndims, null, 1);
     }
     
     
 /* Find all user-defined types for a location. This finds all
  * user-defined types in a group. */
 //int
 //nc_inq_typeids(int ncid, int *ntypes, int *typeids);
 
 /* Are two types equal? */
 //int
 //nc_inq_type_equal(int ncid1, nc_type typeid1, int ncid2, nc_type typeid2,
 //        int *equal);
 
     /* Create a group. its ncid is returned in the new_ncid pointer. */
 //int
 //nc_def_grp(int parent_ncid, const char *name, int *new_ncid);
 
 /* Here are functions for dealing with compound types. */
 
 /* Create a compound type. */
 //int
 //nc_def_compound(int ncid, size_t size, const char *name, nc_type *typeidp);
     public static native int nc_def_compound(int ncid, NativeLong size,
             String name, IntByReference typeidp);
 
 /* Insert a named field into a compound type. */
 //int
 //nc_insert_compound(int ncid, nc_type xtype, const char *name, size_t offset,
 //        nc_type field_typeid);
     public static native int nc_insert_compound(int ncid, int xtype,
             String name, NativeLong offset, int field_typeid);
 
 /* Insert a named array into a compound type. */
 //int
 //nc_insert_array_compound(int ncid, nc_type xtype, const char *name,
 //        size_t offset, nc_type field_typeid, int ndims, const int *dim_sizes);
     private static native int nc_insert_array_compound(int ncid, int xtype,
             String name, NativeLong offset, int field_typeid, int ndims,
             IntBuffer dim_sizes);
     private static native int nc_insert_array_compound(int ncid, int xtype,
             String name, NativeLong offset, int field_typeid, int ndims,
             int[] dim_sizes);
 
     public static int nc_insert_array_compound(int ncid, int xtype,
             String name, NativeLong offset, int field_typeid,
             IntBuffer dim_sizes) {
         return nc_insert_array_compound(ncid, xtype, name, offset, field_typeid,
                 dim_sizes.limit(), dim_sizes);
     }
     public static int nc_insert_array_compound(int ncid, int xtype,
             String name, NativeLong offset, int field_typeid, int... dim_sizes) {
         return nc_insert_array_compound(ncid, xtype, name, offset, field_typeid,
                 dim_sizes.length, dim_sizes);
     }
 
 /* Get the name and size of a type. */
 //int
 //nc_inq_type(int ncid, nc_type xtype, char *name, size_t *size);
 
 /* Get the id of a type from the name. */
 //int
 //nc_inq_typeid(int ncid, const char *name, nc_type *typeidp);
 
 /* Get the name, size, and number of fields in a compound type. */
 //int
 //nc_inq_compound(int ncid, nc_type xtype, char *name, size_t *sizep,
 //        size_t *nfieldsp);
 
 /* Get the name of a compound type. */
 //int
 //nc_inq_compound_name(int ncid, nc_type xtype, char *name);
 
 /* Get the size of a compound type. */
 //int
 //nc_inq_compound_size(int ncid, nc_type xtype, size_t *sizep);
 
 /* Get the number of fields in this compound type. */
 //int
 //nc_inq_compound_nfields(int ncid, nc_type xtype, size_t *nfieldsp);\
 
 /* Given the xtype and the fieldid, get all info about it. */
 //int
 //nc_inq_compound_field(int ncid, nc_type xtype, int fieldid, char *name,
 //        size_t *offsetp, nc_type *field_typeidp, int *ndimsp, int *dim_sizesp);
 
 /* Given the typeid and the fieldid, get the name. */
 //int
 //nc_inq_compound_fieldname(int ncid, nc_type xtype, int fieldid, char *name);
 
 /* Given the xtype and the name, get the fieldid. */
 //int
 //nc_inq_compound_fieldindex(int ncid, nc_type xtype, const char *name,
 //        int *fieldidp);
 
 /* Given the xtype and fieldid, get the offset. */
 //int
 //nc_inq_compound_fieldoffset(int ncid, nc_type xtype, int fieldid,
 //        size_t *offsetp);
 
 /* Given the xtype and the fieldid, get the type of that field. */
 //int
 //nc_inq_compound_fieldtype(int ncid, nc_type xtype, int fieldid,
 //        nc_type *field_typeidp);
 
 /* Given the xtype and the fieldid, get the number of dimensions for
  * that field (scalars are 0). */
 //int
 //nc_inq_compound_fieldndims(int ncid, nc_type xtype, int fieldid,
 //        int *ndimsp);
 
 /* Given the xtype and the fieldid, get the sizes of dimensions for
  * that field. User must have allocated storage for the dim_sizes. */
 //int
 //nc_inq_compound_fielddim_sizes(int ncid, nc_type xtype, int fieldid,
 //        int *dim_sizes);
 
 /* This is the type of arrays of vlens. */
 //typedef struct {
 //    size_t len; /* Length of VL data (in base type units) */
 //    void *p;    /* Pointer to VL data */
 //} nc_vlen_t;
 
 /* This is used when creating a compound type. It calls a mysterious C
  * macro which was found carved into one of the blocks of the
  * Newgrange passage tomb in County Meath, Ireland. This code has been
  * carbon dated to 3200 B.C.E. */
 //#define NC_COMPOUND_OFFSET(S,M)    (offsetof(S,M))
 
 /* Create a variable length type. */
 //int
 //nc_def_vlen(int ncid, const char *name, nc_type base_typeid, nc_type *xtypep);
 
 /* Find out about a vlen. */
 //int
 //nc_inq_vlen(int ncid, nc_type xtype, char *name, size_t *datum_sizep,
 //        nc_type *base_nc_typep);
 
 /* When you read VLEN type the library will actually allocate the
  * storage space for the data. This storage space must be freed, so
  * pass the pointer back to this function, when you're done with the
  * data, and it will free the vlen memory. */
 //int
 //nc_free_vlen(nc_vlen_t *vl);
 
 //int
 //nc_free_vlens(size_t len, nc_vlen_t vlens[]);
 
 /* Put or get one element in a vlen array. */
 //int
 //nc_put_vlen_element(int ncid, int typeid1, void *vlen_element,
 //        size_t len, const void *data);
 
 //int
 //nc_get_vlen_element(int ncid, int typeid1, const void *vlen_element,
 //        size_t *len, void *data);
 
 /* When you read the string type the library will allocate the storage
  * space for the data. This storage space must be freed, so pass the
  * pointer back to this function, when you're done with the data, and
  * it will free the string memory. */
 //int
 //nc_free_string(size_t len, char **data);
 
 /* Find out about a user defined type. */
 //int
 //nc_inq_user_type(int ncid, nc_type xtype, char *name, size_t *size,
 //        nc_type *base_nc_typep, size_t *nfieldsp, int *classp);
 
 /* Write an attribute of any type. */
 //int
 //nc_put_att(int ncid, int varid, const char *name, nc_type xtype,
 //        size_t len, const void *op);
     public static native int nc_put_att(int ncid, int varid, String name, int xtype,
             NativeLong len, Buffer op);
 
 /* Read an attribute of any type. */
 //int
 //nc_get_att(int ncid, int varid, const char *name, void *ip);
     public static native int nc_get_att(int ncid, int varid, String name, Pointer ip);
     
 /* Enum type. */
 
 /* Create an enum type. Provide a base type and a name. At the moment
  * only ints are accepted as base types. */
 //int
 //nc_def_enum(int ncid, nc_type base_typeid, const char *name,
 //        nc_type *typeidp);
 
 /* Insert a named value into an enum type. The value must fit within
  * the size of the enum type, the name size must be <= NC_MAX_NAME. */
 //int
 //nc_insert_enum(int ncid, nc_type xtype, const char *name,
 //        const void *value);
 
 /* Get information about an enum type: its name, base type and the
  * number of members defined. */
 //int
 //nc_inq_enum(int ncid, nc_type xtype, char *name, nc_type *base_nc_typep,
 //        size_t *base_sizep, size_t *num_membersp);
 
 /* Get information about an enum member: a name and value. Name size
  * will be <= NC_MAX_NAME. */
 //int
 //nc_inq_enum_member(int ncid, nc_type xtype, int idx, char *name,
 //        void *value);
 
 
 /* Get enum name from enum value. Name size will be <= NC_MAX_NAME. */
 //int
 //nc_inq_enum_ident(int ncid, nc_type xtype, long long value, char *identifier);
 
 /* Opaque type. */
 
 /* Create an opaque type. Provide a size and a name. */
 //int
 //nc_def_opaque(int ncid, size_t size, const char *name, nc_type *xtypep);
 
 /* Get information about an opaque type. */
 //int
 //nc_inq_opaque(int ncid, nc_type xtype, char *name, size_t *sizep);
 
 /* Write entire var of any type. */
 //int
 //nc_put_var(int ncid, int varid,  const void *op);
 
 /* Read entire var of any type. */
 //int
 //nc_get_var(int ncid, int varid,  void *ip);
 
 /* Write one value. */
 //int
 //nc_put_var1(int ncid, int varid,  const size_t *indexp,
 //        const void *op);
     private static native int nc_put_var1(int ncid, int varid,
             Buffer indexp, Buffer op);
     public static int nc_put_var1(int ncid, int varid,
             Buffer op, NativeLong... indexp) {
         return nc_put_var1(ncid, varid, toBuffer(indexp), op);
     }
 
 /* Read one value. */
 //int
 //nc_get_var1(int ncid, int varid,  const size_t *indexp, void *ip);
 
 /* Write an array of values. */
 //int
 //nc_put_vara(int ncid, int varid,  const size_t *startp,
 //        const size_t *countp, const void *op);
     private static native int nc_put_vara(int ncid, int varid, Buffer startp,
             Buffer countp, Buffer op);
     public static int nc_put_vara(int ncid, int varid, NativeLong[] startp,
             NativeLong[] countp, Buffer op) {
         return nc_put_vara(ncid, varid, toBuffer(startp), toBuffer(countp), op);
     }
     // convenience for 1d variables
     public static native int nc_put_vara(int ncid, int varid,
             NativeLongByReference startp, NativeLongByReference countp,
             Buffer op);
 
 /* Read an array of values. */
 //int
 //nc_get_vara(int ncid, int varid,  const size_t *startp,
 //        const size_t *countp, void *ip);
 
 /* Write slices of an array of values. */
 //int
 //nc_put_vars(int ncid, int varid,  const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const void *op);
 
 /* Read slices of an array of values. */
 //int
 //nc_get_vars(int ncid, int varid,  const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        void *ip);
 
 /* Write mapped slices of an array of values. */
 //int
 //nc_put_varm(int ncid, int varid,  const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, const void *op);
 
 /* Read mapped slices of an array of values. */
 //int
 //nc_get_varm(int ncid, int varid,  const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, void *ip);
 
 /* Extra netcdf-4 stuff. */
 
 /* Set compression settings for a variable. Lower is faster, higher is
  * better. Must be called after nc_def_var and before nc_enddef. */
 //int
 //nc_def_var_deflate(int ncid, int varid, int shuffle, int deflate,
 //        int deflate_level);
 	public static native int nc_def_var_deflate(int ncid, int varid, int shuffle, int deflate, int deflate_level);
 
 /* Find out compression settings of a var. */
 //int
 //nc_inq_var_deflate(int ncid, int varid, int *shufflep,
 //        int *deflatep, int *deflate_levelp);
 
 /* Find out szip settings of a var. */
 //int
 //nc_inq_var_szip(int ncid, int varid, int *options_maskp, int *pixels_per_blockp);
 
 /* Set fletcher32 checksum for a var. This must be done after nc_def_var
    and before nc_enddef. */
 //int
 //nc_def_var_fletcher32(int ncid, int varid, int fletcher32);
 
 /* Inquire about fletcher32 checksum for a var. */
 //int
 //nc_inq_var_fletcher32(int ncid, int varid, int *fletcher32p);
 
 /* Define chunking for a variable. This must be done after nc_def_var
    and before nc_enddef. */
 //int
 //nc_def_var_chunking(int ncid, int varid, int storage, const size_t *chunksizesp);
     private static native int nc_def_var_chunking(int ncid, int varid,
             int storage, Buffer chunksizep);
     public static int nc_def_var_chunking(int ncid, int varid,
             int storage, NativeLong[] chunksizep) {
         return nc_def_var_chunking(ncid, varid, storage, toBuffer(chunksizep));
     }
 
 /* Inq chunking stuff for a var. */
 //int
 //nc_inq_var_chunking(int ncid, int varid, int *storagep, size_t *chunksizesp);
 
 /* Define fill value behavior for a variable. This must be done after
    nc_def_var and before nc_enddef. */
 //int
 //nc_def_var_fill(int ncid, int varid, int no_fill, const void *fill_value);
 
 /* Inq fill value setting for a var. */
 //int
 //nc_inq_var_fill(int ncid, int varid, int *no_fill, void *fill_value);
 
 /* Define the endianness of a variable. */
 //int
 //nc_def_var_endian(int ncid, int varid, int endian);
 
 /* Learn about the endianness of a variable. */
 //int
 //nc_inq_var_endian(int ncid, int varid, int *endianp);
 
 /* Set the fill mode (classic or 64-bit offset files only). */
 //int
 //nc_set_fill(int ncid, int fillmode, int *old_modep);
 
 /* Set the default nc_create format to NC_FORMAT_CLASSIC,
  * NC_FORMAT_64BIT, NC_FORMAT_NETCDF4, NC_FORMAT_NETCDF4_CLASSIC. */
 //int
 //nc_set_default_format(int format, int *old_formatp);
 
 /* Set the cache size, nelems, and preemption policy. */
 //int
 //nc_set_chunk_cache(size_t size, size_t nelems, float preemption);
 
 /* Get the cache size, nelems, and preemption policy. */
 //int
 //nc_get_chunk_cache(size_t *sizep, size_t *nelemsp, float *preemptionp);
 
 /* Set the per-variable cache size, nelems, and preemption policy. */
 //int
 //nc_set_var_chunk_cache(int ncid, int varid, size_t size, size_t nelems,
 //        float preemption);
 
 /* Set the per-variable cache size, nelems, and preemption policy. */
 //int
 //nc_get_var_chunk_cache(int ncid, int varid, size_t *sizep, size_t *nelemsp,
 //        float *preemptionp);
 
 //int
 //nc_redef(int ncid);
 	public static native int nc_redef(int ncid);
 
 //int
 //nc__enddef(int ncid, size_t h_minfree, size_t v_align,
 //        size_t v_minfree, size_t r_align);
 
 //int
 //nc_enddef(int ncid);
     public static native int nc_enddef(int ncid);
 
 //int
 //nc_sync(int ncid);
     public static native int nc_sync(int ncid);
 
 //int
 //nc_abort(int ncid);
 
 //int
 //nc_close(int ncid);
     public static native int nc_close(int ncid);
 
 //int
 //nc_inq(int ncid, int *ndimsp, int *nvarsp, int *nattsp, int *unlimdimidp);
 
 //int
 //nc_inq_ndims(int ncid, int *ndimsp);
 
 //int
 //nc_inq_nvars(int ncid, int *nvarsp);
 
 //int
 //nc_inq_natts(int ncid, int *nattsp);
 
 //int
 //nc_inq_unlimdim(int ncid, int *unlimdimidp);
     public static native int nc_inq_unlimdim(int ncid, IntByReference unlimdimidp);
 
 /* The next function is for NetCDF-4 only */
 //int
 //nc_inq_unlimdims(int ncid, int *nunlimdimsp, int *unlimdimidsp);
     public static native int nc_inq_unlimdims(int ncid, IntByReference nunlimdimsp, int[] unlimdimidp);
 
 /* Added in 3.6.1 to return format of netCDF file. */
 //int
 //nc_inq_format(int ncid, int *formatp);
 
 /* Begin _dim */
 
 //int
 //nc_def_dim(int ncid, const char *name, size_t len, int *idp);
     public static native int nc_def_dim(int ncid, String name, NativeLong len,
             IntByReference idp);
 
 //int
 //nc_inq_dimid(int ncid, const char *name, int *idp);
 	public static native int nc_inq_dimid(int ncid, String name, IntByReference idp);
 
 //int
 //nc_inq_dim(int ncid, int dimid, char *name, size_t *lenp);
 
 //int
 //nc_inq_dimname(int ncid, int dimid, char *name);
 	public static native int nc_inq_dimname(int ncid, int dimid, ByteBuffer name);
 
 //int
 //nc_inq_dimlen(int ncid, int dimid, size_t *lenp);
 	public static native int nc_inq_dimlen(int ncid, int dimId, NativeLongByReference lenp);
 
 //int
 //nc_rename_dim(int ncid, int dimid, const char *name);
 
 /* End _dim */
 /* Begin _att */
 
 //int
 //nc_inq_att(int ncid, int varid, const char *name,
 //        nc_type *xtypep, size_t *lenp);
 	public static native int nc_inq_att(int ncid, int varid, byte[] name,
 			IntByReference xtypep, NativeLongByReference lenp);
 
 //int
 //nc_inq_attid(int ncid, int varid, const char *name, int *idp);
 
 //int
 //nc_inq_atttype(int ncid, int varid, const char *name, nc_type *xtypep);
 
 //int
 //nc_inq_attlen(int ncid, int varid, const char *name, size_t *lenp);
 
 //int
 //nc_inq_attname(int ncid, int varid, int attnum, char *name);
 	public static native int nc_inq_attname(int ncid, int varid, int attnum, ByteBuffer name);
 
 //int
 //nc_copy_att(int ncid_in, int varid_in, const char *name, int ncid_out, int varid_out);
 
 //int
 //nc_rename_att(int ncid, int varid, const char *name, const char *newname);
 
 //int
 //nc_del_att(int ncid, int varid, const char *name);
 
 /* End _att */
 /* Begin {put,get}_att */
 
 //int
 //nc_put_att_text(int ncid, int varid, const char *name,
 //        size_t len, const char *op);
     private static native int nc_put_att_text(int ncid, int varid, String name,
             NativeLong len, String op);
     public static int nc_put_att_text(int ncid, int varid, String name, String op) {
         return nc_put_att_text(ncid, varid, name, new NativeLong(op.length()), op);
     }
 
 //int
 //nc_get_att_text(int ncid, int varid, const char *name, char *ip);
 
 //int
 //nc_put_att_uchar(int ncid, int varid, const char *name, nc_type xtype,
 //        size_t len, const unsigned char *op);
     private static native int nc_put_att_uchar(int ncid, int varid, String name,
             int xtype, NativeLong len, ByteBuffer op);
     private static native int nc_put_att_uchar(int ncid, int varid, String name,
             int xtype, NativeLong len, byte[] op);
 
     public static int nc_put_att_uchar(int ncid, int varid, String name,
             int xtype, ByteBuffer op) {
         return nc_put_att_uchar(ncid, varid, name, xtype,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_uchar(int ncid, int varid, String name,
             int xtype, byte[] op) {
         return nc_put_att_uchar(ncid, varid, name, xtype, new NativeLong(op.length), op);
     }
 
     public static int nc_put_att_uchar(int ncid, int varid, String name,
             ByteBuffer op) {
         return nc_put_att_uchar(ncid, varid, name, NC_CHAR,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_uchar(int ncid, int varid, String name,
             byte[] op) {
         return nc_put_att_uchar(ncid, varid, name, NC_CHAR,
                 new NativeLong(op.length), op);
     }
 
     public static int nc_put_att_uchar(int ncid, int varid, String name,
             int xtype, byte op) {
         return nc_put_att_uchar(ncid, varid, name, xtype, new byte[] { op });
     }
     public static int nc_put_att_uchar(int ncid, int varid, String name,
             byte op) {
         return nc_put_att_uchar(ncid, varid, name, NC_CHAR, op);
     }
 
 //int
 //nc_get_att_uchar(int ncid, int varid, const char *name, unsigned char *ip);
 
 //int
 //nc_put_att_schar(int ncid, int varid, const char *name, nc_type xtype,
 //        size_t len, const signed char *op);
     private static native int nc_put_att_schar(int ncid, int varid, String name,
             int xtype, NativeLong len, ByteBuffer op);
     private static native int nc_put_att_schar(int ncid, int varid, String name,
             int xtype, NativeLong len, byte[] op);
 
     public static int nc_put_att_schar(int ncid, int varid, String name,
             int xtype, ByteBuffer op) {
         return nc_put_att_schar(ncid, varid, name, xtype,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_schar(int ncid, int varid, String name,
             int xtype, byte[] op) {
         return nc_put_att_schar(ncid, varid, name, xtype,
                 new NativeLong(op.length), op);
     }
 
 
     public static int nc_put_att_schar(int ncid, int varid, String name,
             ByteBuffer op) {
         return nc_put_att_schar(ncid, varid, name, NC_CHAR,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_schar(int ncid, int varid, String name,
             byte[] op) {
         return nc_put_att_schar(ncid, varid, name, NC_CHAR,
                 new NativeLong(op.length), op);
     }
 
     public static int nc_put_att_schar(int ncid, int varid, String name,
             int xtype, byte op) {
         return nc_put_att_schar(ncid, varid, name, xtype, new byte[] { op });
     }
     public static int nc_put_att_schar(int ncid, int varid, String name,
             byte op) {
         return nc_put_att_schar(ncid, varid, name, NC_CHAR, op);
     }
 
 //int
 //nc_get_att_schar(int ncid, int varid, const char *name, signed char *ip);
 
 //int
 //nc_put_att_short(int ncid, int varid, const char *name, nc_type xtype,
 //        size_t len, const short *op);
     private static native int nc_put_att_short(int ncid, int varid, String name,
             int xtype, NativeLong len, ShortBuffer op);
     private static native int nc_put_att_short(int ncid, int varid, String name,
             int xtype, NativeLong len, short[] op);
 
     public static int nc_put_att_short(int ncid, int varid, String name,
             int xtype, ShortBuffer op) {
         return nc_put_att_short(ncid, varid, name, xtype,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_short(int ncid, int varid, String name,
             int xtype, short[] op) {
         return nc_put_att_short(ncid, varid, name, xtype,
                 new NativeLong(op.length), op);
     }
 
 
     public static int nc_put_att_short(int ncid, int varid, String name,
             ShortBuffer op) {
         return nc_put_att_short(ncid, varid, name, NC_SHORT,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_short(int ncid, int varid, String name,
             short[] op) {
         return nc_put_att_short(ncid, varid, name, NC_SHORT,
                 new NativeLong(op.length), op);
     }
 
     public static int nc_put_att_short(int ncid, int varid, String name,
             int xtype, short op) {
         return nc_put_att_short(ncid, varid, name, xtype, new short[] { op });
     }
     public static int nc_put_att_short(int ncid, int varid, String name,
             short op) {
         return nc_put_att_short(ncid, varid, name, NC_SHORT, op);
     }
 
 //int
 //nc_get_att_short(int ncid, int varid, const char *name, short *ip);
 
 //int
 //nc_put_att_int(int ncid, int varid, const char *name, nc_type xtype,
 //        size_t len, const int *op);
     private static native int nc_put_att_int(int ncid, int varid, String name,
             int xtype, NativeLong len, IntBuffer op);
     private static native int nc_put_att_int(int ncid, int varid, String name,
             int xtype, NativeLong len, int[] op);
 
     public static int nc_put_att_int(int ncid, int varid, String name,
             int xtype, IntBuffer op) {
         return nc_put_att_int(ncid, varid, name, xtype,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_int(int ncid, int varid, String name,
             int xtype, int[] op) {
         return nc_put_att_int(ncid, varid, name, xtype,
                 new NativeLong(op.length), op);
     }
 
 
     public static int nc_put_att_int(int ncid, int varid, String name,
             IntBuffer op) {
         return nc_put_att_int(ncid, varid, name, NC_INT,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_int(int ncid, int varid, String name,
             int[] op) {
         return nc_put_att_int(ncid, varid, name, NC_INT,
                 new NativeLong(op.length), op);
     }
 
     public static int nc_put_att_int(int ncid, int varid, String name,
             int xtype, int op) {
         return nc_put_att_int(ncid, varid, name, xtype, new int[] { op });
     }
     public static int nc_put_att_int(int ncid, int varid, String name,
             int op) {
         return nc_put_att_int(ncid, varid, name, NC_INT, op);
     }
 
 //int
 //nc_get_att_int(int ncid, int varid, const char *name, int *ip);
 
 //int
 //nc_put_att_long(int ncid, int varid, const char *name, nc_type xtype,
 //        size_t len, const long *op);
 
 //int
 //nc_get_att_long(int ncid, int varid, const char *name, long *ip);
 
 //int
 //nc_put_att_float(int ncid, int varid, const char *name, nc_type xtype,
 //        size_t len, const float *op);
     private static native int nc_put_att_float(int ncid, int varid, String name,
             int xtype, NativeLong len, FloatBuffer op);
     private static native int nc_put_att_float(int ncid, int varid, String name,
             int xtype, NativeLong len, float[] op);
 
     public static int nc_put_att_float(int ncid, int varid, String name,
             int xtype, FloatBuffer op) {
         return nc_put_att_float(ncid, varid, name, xtype,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_float(int ncid, int varid, String name,
             int xtype, float[] op) {
         return nc_put_att_float(ncid, varid, name, xtype,
                 new NativeLong(op.length), op);
     }
 
 
     public static int nc_put_att_float(int ncid, int varid, String name,
             FloatBuffer op) {
         return nc_put_att_float(ncid, varid, name, NC_FLOAT,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_float(int ncid, int varid, String name,
             float[] op) {
         return nc_put_att_float(ncid, varid, name, NC_FLOAT,
                 new NativeLong(op.length), op);
     }
 
     public static int nc_put_att_float(int ncid, int varid, String name,
             int xtype, float op) {
         return nc_put_att_float(ncid, varid, name, xtype, new float[] { op });
     }
     public static int nc_put_att_float(int ncid, int varid, String name,
             float op) {
         return nc_put_att_float(ncid, varid, name, NC_FLOAT, op);
     }
 
 //int
 //nc_get_att_float(int ncid, int varid, const char *name, float *ip);
 
 //int
 //nc_put_att_double(int ncid, int varid, const char *name, nc_type xtype,
 //        size_t len, const double *op);
     private static native int nc_put_att_double(int ncid, int varid, String name,
             int xtype, NativeLong len, DoubleBuffer op);
     private static native int nc_put_att_double(int ncid, int varid, String name,
             int xtype, NativeLong len, double[] op);
 
     public static int nc_put_att_double(int ncid, int varid, String name,
             int xtype, DoubleBuffer op) {
         return nc_put_att_double(ncid, varid, name, xtype,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_double(int ncid, int varid, String name,
             int xtype, double[] op) {
         return nc_put_att_double(ncid, varid, name, xtype,
                 new NativeLong(op.length), op);
     }
     public static int nc_put_att_double(int ncid, int varid, String name,
             int xtype, double op) {
         return nc_put_att_double(ncid, varid, name, xtype, new double[] { op });
     }
 
 
     public static int nc_put_att_double(int ncid, int varid, String name,
             DoubleBuffer op) {
         return nc_put_att_double(ncid, varid, name, NC_DOUBLE,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_double(int ncid, int varid, String name,
             double[] op) {
         return nc_put_att_double(ncid, varid, name, NC_DOUBLE,
                 new NativeLong(op.length), op);
     }
     public static int nc_put_att_double(int ncid, int varid, String name,
             double op) {
         return nc_put_att_double(ncid, varid, name, NC_DOUBLE, op);
     }
 
 //int
 //nc_get_att_double(int ncid, int varid, const char *name, double *ip);
 
 //int
 //nc_put_att_ubyte(int ncid, int varid, const char *name, nc_type xtype,
 //        size_t len, const unsigned char *op);
     private static native int nc_put_att_ubyte(int ncid, int varid, String name,
             int xtype, NativeLong len, ByteBuffer op);
     private static native int nc_put_att_ubyte(int ncid, int varid, String name,
             int xtype, NativeLong len, byte[] op);
 
     public static int nc_put_att_ubyte(int ncid, int varid, String name,
             int xtype, ByteBuffer op) {
         return nc_put_att_ubyte(ncid, varid, name, xtype,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_ubyte(int ncid, int varid, String name,
             int xtype, byte[] op) {
         return nc_put_att_ubyte(ncid, varid, name, xtype,
                 new NativeLong(op.length), op);
     }
     public static int nc_put_att_ubyte(int ncid, int varid, String name,
             int xtype, byte op) {
         return nc_put_att_ubyte(ncid, varid, name, xtype, new byte[] { op });
     }
 
 
     public static int nc_put_att_ubyte(int ncid, int varid, String name,
             ByteBuffer op) {
         return nc_put_att_ubyte(ncid, varid, name, NC_BYTE,
                 new NativeLong(op.limit()), op);
     }
     public static int nc_put_att_ubyte(int ncid, int varid, String name,
             byte[] op) {
         return nc_put_att_ubyte(ncid, varid, name, NC_BYTE,
                 new NativeLong(op.length), op);
     }
     public static int nc_put_att_ubyte(int ncid, int varid, String name,
             byte op) {
         return nc_put_att_ubyte(ncid, varid, name, NC_BYTE, op);
     }
 
 //int
 //nc_get_att_ubyte(int ncid, int varid, const char *name,
 //        unsigned char *ip);
 
 //int
 //nc_put_att_ushort(int ncid, int varid, const char *name, nc_type xtype,
 //        size_t len, const unsigned short *op);
 
 //int
 //nc_get_att_ushort(int ncid, int varid, const char *name, unsigned short *ip);
 
 //int
 //nc_put_att_uint(int ncid, int varid, const char *name, nc_type xtype,
 //        size_t len, const unsigned int *op);
 
 //int
 //nc_get_att_uint(int ncid, int varid, const char *name, unsigned int *ip);
 
 //int
 //nc_put_att_longlong(int ncid, int varid, const char *name, nc_type xtype,
 //        size_t len, const long long *op);
 
 //int
 //nc_get_att_longlong(int ncid, int varid, const char *name, long long *ip);
 
 //int
 //nc_put_att_ulonglong(int ncid, int varid, const char *name, nc_type xtype,
 //        size_t len, const unsigned long long *op);
 
 //int
 //nc_get_att_ulonglong(int ncid, int varid, const char *name,
 //        unsigned long long *ip);
 
 //int
 //nc_put_att_string(int ncid, int varid, const char *name,
 //        size_t len, const char **op);
 
 //int
 //nc_get_att_string(int ncid, int varid, const char *name, char **ip);
 
 /* End {put,get}_att */
 /* Begin _var */
 
 //int
 //nc_def_var(int ncid, const char *name, nc_type xtype, int ndims,
 //        const int *dimidsp, int *varidp);
     private static native int nc_def_var(int ncid, String name, int xtype,
             int ndims, IntBuffer dimidsp, IntByReference varidp);
     private static native int nc_def_var(int ncid, String name, int xtype,
             int ndims, int[] dimidsp, IntByReference varidp);
 
     public static int nc_def_var(int ncid, String name, int xtype,
             IntBuffer dimidsp, IntByReference varidp) {
         return nc_def_var(ncid, name, xtype, dimidsp.limit(), dimidsp, varidp);
     }
     public static int nc_def_var(int ncid, String name, int xtype,
             int[] dimidsp, IntByReference varidp) {
         return nc_def_var(ncid, name, xtype, dimidsp.length, dimidsp, varidp);
     }
 
 //int
 //nc_inq_var(int ncid, int varid, char *name, nc_type *xtypep,
 //        int *ndimsp, int *dimidsp, int *nattsp);
     public static native int nc_inq_var(int ncid, int varid, ByteBuffer name, IntByReference xtypep, 
     		IntByReference ndimsp, int[] dimidsp, IntByReference nattsp);
     
 //int
 //nc_inq_varid(int ncid, const char *name, int *varidp);
 	public static native int nc_inq_varid(int ncid, String name, IntByReference varidp);
 
 //int
 //nc_inq_varname(int ncid, int varid, char *name);
 	public static native int nc_inq_varname(int ncid, int varid, ByteBuffer name);
 //int
 //nc_inq_vartype(int ncid, int varid, nc_type *xtypep);
 	public static native int nc_inq_vartype(int ncid, int varid, IntByReference xtypep);
 
 //int
 //nc_inq_varndims(int ncid, int varid, int *ndimsp);
 	public static native int nc_inq_varndims(int ncid, int varid, IntByReference ndimsp);
 
 //int
 //nc_inq_vardimid(int ncid, int varid, int *dimidsp);
 	public static native int nc_inq_vardimid(int ncid, int varid, IntBuffer dimidsp);
 	public static native int nc_inq_vardimid(int ncid, int varid, int[] dimidsp);
 
 //int
 //nc_inq_varnatts(int ncid, int varid, int *nattsp);
	public static native int nc_inq_varnatts(int ncid, int varid, IntByReference nattsp);
 
 //int
 //nc_rename_var(int ncid, int varid, const char *name);
 
 //int
 //nc_copy_var(int ncid_in, int varid, int ncid_out);
 	public static native int nc_copy_var(int ncid_in, int varid, int ncid_out);
 
 //#ifndef ncvarcpy
 ///* support the old name for now */
 //#define ncvarcpy(ncid_in, varid, ncid_out) ncvarcopy((ncid_in), (varid), (ncid_out))
 //#endif
 
 /* End _var */
 /* Begin {put,get}_var1 */
 
 //int
 //nc_put_var1_text(int ncid, int varid, const size_t *indexp, const char *op);
     private static native int nc_put_var1_text(int ncid, int varid,
             Buffer indexp, String op);
     public static int nc_put_var1_text(int ncid, int varid, String op, NativeLong... indexp) {
         return nc_put_var1_text(ncid, varid, toBuffer(indexp), op);
     }
 
 //int
 //nc_get_var1_text(int ncid, int varid, const size_t *indexp, char *ip);
 
 //int
 //nc_put_var1_uchar(int ncid, int varid, const size_t *indexp,
 //        const unsigned char *op);
     private static native int nc_put_var1_uchar(int ncid, int varid,
             Buffer indexp, ByteByReference op);
     private static native int nc_put_var1_uchar(int ncid, int varid,
             Buffer indexp, byte[] op);
 
     public static int nc_put_var1_uchar(int ncid, int varid,
             ByteByReference op, NativeLong... indexp) {
         return nc_put_var1_uchar(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_uchar(int ncid, int varid,
             byte[] op, NativeLong... indexp) {
         return nc_put_var1_uchar(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_uchar(int ncid, int varid,
             byte op, NativeLong... indexp) {
         return nc_put_var1_uchar(ncid, varid, toBuffer(indexp), new byte[] { op });
     }
 
 //int
 //nc_get_var1_uchar(int ncid, int varid, const size_t *indexp,
 //        unsigned char *ip);
 
 //int
 //nc_put_var1_schar(int ncid, int varid, const size_t *indexp,
 //        const signed char *op);
     private static native int nc_put_var1_schar(int ncid, int varid,
             Buffer indexp, ByteByReference op);
     private static native int nc_put_var1_schar(int ncid, int varid,
             Buffer indexp, byte[] op);
 
     public static int nc_put_var1_schar(int ncid, int varid,
             ByteByReference op, NativeLong... indexp) {
         return nc_put_var1_schar(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_schar(int ncid, int varid,
             byte[] op, NativeLong... indexp) {
         return nc_put_var1_schar(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_schar(int ncid, int varid,
             byte op, NativeLong... indexp) {
         return nc_put_var1_schar(ncid, varid, toBuffer(indexp), new byte[] { op });
     }
 
 //int
 //nc_get_var1_schar(int ncid, int varid, const size_t *indexp,
 //        signed char *ip);
 
 //int
 //nc_put_var1_short(int ncid, int varid, const size_t *indexp,
 //        const short *op);
     public static native int nc_put_var1_short(int ncid, int varid,
             Buffer indexp, ShortByReference op);
     public static native int nc_put_var1_short(int ncid, int varid,
             Buffer indexp, short[] op);
 
     public static int nc_put_var1_short(int ncid, int varid,
             ShortByReference op, NativeLong... indexp) {
         return nc_put_var1_short(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_short(int ncid, int varid,
             short[] op, NativeLong... indexp) {
         return nc_put_var1_short(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_short(int ncid, int varid,
             short op, NativeLong... indexp) {
         return nc_put_var1_short(ncid, varid, toBuffer(indexp), new short[] { op });
     }
 
 //int
 //nc_get_var1_short(int ncid, int varid, const size_t *indexp,
 //        short *ip);
 
 //int
 //nc_put_var1_int(int ncid, int varid, const size_t *indexp, const int *op);
     private static native int nc_put_var1_int(int ncid, int varid,
             Buffer indexp, IntByReference op);
     private static native int nc_put_var1_int(int ncid, int varid,
             Buffer indexp, int[] op);
 
     public static int nc_put_var1_int(int ncid, int varid,
             IntByReference op, NativeLong... indexp) {
         return nc_put_var1_int(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_int(int ncid, int varid,
             int[] op, NativeLong... indexp) {
         return nc_put_var1_int(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_int(int ncid, int varid,
             int op, NativeLong... indexp) {
         return nc_put_var1_int(ncid, varid, toBuffer(indexp), new int[] { op });
     }
 
 //int
 //nc_get_var1_int(int ncid, int varid, const size_t *indexp, int *ip);
 
 //int
 //nc_put_var1_long(int ncid, int varid, const size_t *indexp, const long *op);
 
 //int
 //nc_get_var1_long(int ncid, int varid, const size_t *indexp, long *ip);
 
 //int
 //nc_put_var1_float(int ncid, int varid, const size_t *indexp, const float *op);
     private static native int nc_put_var1_float(int ncid, int varid,
             Buffer indexp, FloatByReference op);
     private static native int nc_put_var1_float(int ncid, int varid,
             Buffer indexp, float[] op);
 
     public static int nc_put_var1_float(int ncid, int varid,
             FloatByReference op, NativeLong... indexp) {
         return nc_put_var1_float(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_float(int ncid, int varid,
             float[] op, NativeLong... indexp) {
         return nc_put_var1_float(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_float(int ncid, int varid,
             float op, NativeLong... indexp) {
         return nc_put_var1_float(ncid, varid, toBuffer(indexp), new float[] { op });
     }
 
 //int
 //nc_get_var1_float(int ncid, int varid, const size_t *indexp, float *ip);
 
 //int
 //nc_put_var1_double(int ncid, int varid, const size_t *indexp, const double *op);
     private static native int nc_put_var1_double(int ncid, int varid,
             Buffer indexp, DoubleByReference op);
     private static native int nc_put_var1_double(int ncid, int varid,
             Buffer indexp, double[] op);
 
     public static int nc_put_var1_double(int ncid, int varid,
             DoubleByReference op, NativeLong... indexp) {
         return nc_put_var1_double(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_double(int ncid, int varid,
             double[] op, NativeLong... indexp) {
         return nc_put_var1_double(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_double(int ncid, int varid,
             double op, NativeLong... indexp) {
         return nc_put_var1_double(ncid, varid, toBuffer(indexp), new double[] { op });
     }
 
 //int
 //nc_get_var1_double(int ncid, int varid, const size_t *indexp, double *ip);
 
 //int
 //nc_put_var1_ubyte(int ncid, int varid, const size_t *indexp,
 //        const unsigned char *op);
     private static native int nc_put_var1_ubyte(int ncid, int varid,
             Buffer indexp, ByteByReference op);
     private static native int nc_put_var1_ubyte(int ncid, int varid,
             Buffer indexp, byte[] op);
 
     public static int nc_put_var1_ubyte(int ncid, int varid,
             ByteByReference op, NativeLong... indexp) {
         return nc_put_var1_ubyte(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_ubyte(int ncid, int varid,
             byte[] op, NativeLong... indexp) {
         return nc_put_var1_ubyte(ncid, varid, toBuffer(indexp), op);
     }
     public static int nc_put_var1_ubyte(int ncid, int varid,
             byte op, NativeLong... indexp) {
         return nc_put_var1_ubyte(ncid, varid, toBuffer(indexp), new byte[] { op });
     }
 
 //int
 //nc_get_var1_ubyte(int ncid, int varid, const size_t *indexp,
 //        unsigned char *ip);
 
 //int
 //nc_put_var1_ushort(int ncid, int varid, const size_t *indexp,
 //        const unsigned short *op);
 
 //int
 //nc_get_var1_ushort(int ncid, int varid, const size_t *indexp,
 //        unsigned short *ip);
 
 //int
 //nc_put_var1_uint(int ncid, int varid, const size_t *indexp,
 //        const unsigned int *op);
 
 //int
 //nc_get_var1_uint(int ncid, int varid, const size_t *indexp,
 //        unsigned int *ip);
 
 //int
 //nc_put_var1_longlong(int ncid, int varid, const size_t *indexp,
 //        const long long *op);
 
 //int
 //nc_get_var1_longlong(int ncid, int varid, const size_t *indexp,
 //        long long *ip);
 
 //int
 //nc_put_var1_ulonglong(int ncid, int varid, const size_t *indexp,
 //        const unsigned long long *op);
 
 //int
 //nc_get_var1_ulonglong(int ncid, int varid, const size_t *indexp,
 //        unsigned long long *ip);
 
 //int
 //nc_put_var1_string(int ncid, int varid, const size_t *indexp,
 //        const char **op);
 
 //int
 //nc_get_var1_string(int ncid, int varid, const size_t *indexp,
 //        char **ip);
 
 /* End {put,get}_var1 */
 /* Begin {put,get}_vara */
 
 //int
 //nc_put_vara_text(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const char *op);
     private static native int nc_put_vara_text(int ncid, int varid,
             Buffer startp, Buffer countp, String op);
     public static int nc_put_vara_text(int ncid, int varid, String op,
             NativeLong... startp) {
         return nc_put_vara_text(ncid, varid, toBuffer(startp),
                 NativeLong.SIZE == 4 ?
                     IntBuffer.wrap(new int[] { 1, op.length() }) :
                     LongBuffer.wrap(new long[] {1, op.length() }),
                 op);
     }
 
 //int
 //nc_get_vara_text(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, char *ip);
 
 //int
 //nc_put_vara_uchar(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const unsigned char *op);
 
 //int
 //nc_get_vara_uchar(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, unsigned char *ip);
 
 //int
 //nc_put_vara_schar(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const signed char *op);
 
 //int
 //nc_get_vara_schar(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, signed char *ip);
 
 //int
 //nc_put_vara_short(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const short *op);
 
 //int
 //nc_get_vara_short(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, short *ip);
 
 //int
 //nc_put_vara_int(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const int *op);
 
 //int
 //nc_get_vara_int(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, int *ip);
 
 //int
 //nc_put_vara_long(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const long *op);
 
 //int
 //nc_get_vara_long(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, long *ip);
 
 //int
 //nc_put_vara_float(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const float *op);
 
 //int
 //nc_get_vara_float(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, float *ip);
 
 //int
 //nc_put_vara_double(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const double *op);
 
 //int
 //nc_get_vara_double(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, double *ip);
 
 //int
 //nc_put_vara_ubyte(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const unsigned char *op);
 
 //int
 //nc_get_vara_ubyte(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, unsigned char *ip);
 
 //int
 //nc_put_vara_ushort(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const unsigned short *op);
 
 //int
 //nc_get_vara_ushort(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, unsigned short *ip);
 
 //int
 //nc_put_vara_uint(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const unsigned int *op);
 
 //int
 //nc_get_vara_uint(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, unsigned int *ip);
 
 //int
 //nc_put_vara_longlong(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const long long *op);
 
 //int
 //nc_get_vara_longlong(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, long long *ip);
 
 //int
 //nc_put_vara_ulonglong(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const unsigned long long *op);
 
 //int
 //nc_get_vara_ulonglong(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, unsigned long long *ip);
 
 //int
 //nc_put_vara_string(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const char **op);
 
 //int
 //nc_get_vara_string(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, char **ip);
 
 /* End {put,get}_vara */
 /* Begin {put,get}_vars */
 
 //int
 //nc_put_vars_text(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        const char *op);
 
 //int
 //nc_get_vars_text(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        char *ip);
 
 //int
 //nc_put_vars_uchar(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        const unsigned char *op);
 
 //int
 //nc_get_vars_uchar(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        unsigned char *ip);
 
 //int
 //nc_put_vars_schar(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        const signed char *op);
 
 //int
 //nc_get_vars_schar(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        signed char *ip);
 
 //int
 //nc_put_vars_short(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        const short *op);
 
 //int
 //nc_get_vars_short(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        short *ip);
 
 //int
 //nc_put_vars_int(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        const int *op);
 
 //int
 //nc_get_vars_int(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        int *ip);
 
 //int
 //nc_put_vars_long(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        const long *op);
 
 //int
 //nc_get_vars_long(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        long *ip);
 
 //int
 //nc_put_vars_float(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        const float *op);
 
 //int
 //nc_get_vars_float(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        float *ip);
 
 //int
 //nc_put_vars_double(int ncid, int varid,
 //        const size_t *startp, const size_t *countp, const ptrdiff_t *stridep,
 //        const double *op);
 
 //int
 //nc_get_vars_double(int ncid, int varid,	const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        double *ip);
 
 //int
 //nc_put_vars_ubyte(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const unsigned char *op);
 
 //int
 //nc_get_vars_ubyte(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        unsigned char *ip);
 
 //int
 //nc_put_vars_ushort(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const unsigned short *op);
 
 //int
 //nc_get_vars_ushort(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        unsigned short *ip);
 
 //int
 //nc_put_vars_uint(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const unsigned int *op);
 
 //int
 //nc_get_vars_uint(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        unsigned int *ip);
 
 //int
 //nc_put_vars_longlong(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const long long *op);
 
 //int
 //nc_get_vars_longlong(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        long long *ip);
 
 //int
 //nc_put_vars_ulonglong(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const unsigned long long *op);
 
 //int
 //nc_get_vars_ulonglong(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        unsigned long long *ip);
 
 //int
 //nc_put_vars_string(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const char **op);
 
 //int
 //nc_get_vars_string(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        char **ip);
 
 /* End {put,get}_vars */
 /* Begin {put,get}_varm */
 
 //int
 //nc_put_varm_text(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, const char *op);
 
 //int
 //nc_get_varm_text(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, char *ip);
 
 //int
 //nc_put_varm_uchar(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, const unsigned char *op);
 
 //int
 //nc_get_varm_uchar(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, unsigned char *ip);
 
 //int
 //nc_put_varm_schar(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, const signed char *op);
 
 //int
 //nc_get_varm_schar(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, signed char *ip);
 
 //int
 //nc_put_varm_short(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, const short *op);
 
 //int
 //nc_get_varm_short(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, short *ip);
 
 //int
 //nc_put_varm_int(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, const int *op);
 
 //int
 //nc_get_varm_int(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, int *ip);
 
 //int
 //nc_put_varm_long(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, const long *op);
 
 //int
 //nc_get_varm_long(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, long *ip);
 
 //int
 //nc_put_varm_float(int ncid, int varid,const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, const float *op);
 
 //int
 //nc_get_varm_float(int ncid, int varid,const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, float *ip);
 
 //int
 //nc_put_varm_double(int ncid, int varid,	const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t *imapp, const double *op);
 
 //int
 //nc_get_varm_double(int ncid, int varid,	const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t * imapp, double *ip);
 
 //int
 //nc_put_varm_ubyte(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t * imapp, const unsigned char *op);
 
 //int
 //nc_get_varm_ubyte(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t * imapp, unsigned char *ip);
 
 //int
 //nc_put_varm_ushort(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t * imapp, const unsigned short *op);
 
 //int
 //nc_get_varm_ushort(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t * imapp, unsigned short *ip);
 
 //int
 //nc_put_varm_uint(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t * imapp, const unsigned int *op);
 
 //int
 //nc_get_varm_uint(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t * imapp, unsigned int *ip);
 
 //int
 //nc_put_varm_longlong(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t * imapp, const long long *op);
 
 //int
 //nc_get_varm_longlong(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t * imapp, long long *ip);
 
 //int
 //nc_put_varm_ulonglong(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t * imapp, const unsigned long long *op);
 
 //int
 //nc_get_varm_ulonglong(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t * imapp, unsigned long long *ip);
 
 //int
 //nc_put_varm_string(int ncid, int varid, const size_t *startp,
 //        const size_t *countp, const ptrdiff_t *stridep,
 //        const ptrdiff_t * imapp, const char **op);
 
 //int
 //nc_get_varm_string(int ncid, int varid, const size_t *startp,
 //       const size_t *countp, const ptrdiff_t *stridep,
 //       const ptrdiff_t * imapp, char **ip);
 
 /* End {put,get}_varm */
 /* Begin {put,get}_var */
 
 //int
 //nc_put_var_text(int ncid, int varid, const char *op);
     public static native int nc_put_var_text(int ncid, int varid, String op);
 
 //int
 //nc_get_var_text(int ncid, int varid, char *ip);
 
 //int
 //nc_put_var_uchar(int ncid, int varid, const unsigned char *op);
     public static native int nc_put_var_uchar(int ncid, int varid, ByteByReference op);
     public static native int nc_put_var_uchar(int ncid, int varid, ByteBuffer op);
     public static native int nc_put_var_uchar(int ncid, int varid, byte[] op);
     public static int nc_put_var_uchar(int ncid, int varid, byte op) {
         return nc_put_var_uchar(ncid, varid, new byte[] { op });
     }
 
 //int
 //nc_get_var_uchar(int ncid, int varid, unsigned char *ip);
 
 //int
 //nc_put_var_schar(int ncid, int varid, const signed char *op);
     public static native int nc_put_var_schar(int ncid, int varid, ByteByReference op);
     public static native int nc_put_var_schar(int ncid, int varid, ByteBuffer op);
     public static native int nc_put_var_schar(int ncid, int varid, byte[] op);
     public static int nc_put_var_schar(int ncid, int varid, byte op) {
         return nc_put_var_schar(ncid, varid, new byte[] { op });
     }
 
 //int
 //nc_get_var_schar(int ncid, int varid, signed char *ip);
 
 //int
 //nc_put_var_short(int ncid, int varid, const short *op);
     public static native int nc_put_var_short(int ncid, int varid, ShortByReference op);
     public static native int nc_put_var_short(int ncid, int varid, ShortBuffer op);
     public static native int nc_put_var_short(int ncid, int varid, short[] op);
     public static int nc_put_var_short(int ncid, int varid, short op) {
         return nc_put_var_short(ncid, varid, new short[] { op });
     }
 
 //int
 //nc_get_var_short(int ncid, int varid, short *ip);
 
 //int
 //nc_put_var_int(int ncid, int varid, const int *op);
     public static native int nc_put_var_int(int ncid, int varid, IntByReference op);
     public static native int nc_put_var_int(int ncid, int varid, IntBuffer op);
     public static native int nc_put_var_int(int ncid, int varid, int[] op);
     public static int nc_put_var_int(int ncid, int varid, int op) {
         return nc_put_var_int(ncid, varid, new int[] { op });
     }
 
 //int
 //nc_get_var_int(int ncid, int varid, int *ip);
 
 //int
 //nc_put_var_long(int ncid, int varid, const long *op);
 
 //int
 //nc_get_var_long(int ncid, int varid, long *ip);
 
 //int
 //nc_put_var_float(int ncid, int varid, const float *op);
     public static native int nc_put_var_float(int ncid, int varid, Pointer op);
     public static native int nc_put_var_float(int ncid, int varid, FloatByReference op);
     public static native int nc_put_var_float(int ncid, int varid, FloatBuffer op);
     public static native int nc_put_var_float(int ncid, int varid, float[] op);
     public static int nc_put_var_float(int ncid, int varid, float op) {
         return nc_put_var_float(ncid, varid, new float[] { op });
     }
 
 //int
 //nc_get_var_float(int ncid, int varid, float *ip);
 	public static native int nc_get_var_float(int ncid, int varid, Pointer ip);
 	public static native int nc_get_var_float(int ncid, int varid, FloatBuffer ip);
 	public static native int nc_get_var_float(int ncid, int varid, float[] ip);
 
 //int
 //nc_put_var_double(int ncid, int varid, const double *op);
     public static native int nc_put_var_double(int ncid, int varid, DoubleByReference op);
     public static native int nc_put_var_double(int ncid, int varid, DoubleBuffer op);
     public static native int nc_put_var_double(int ncid, int varid, double[] op);
     public static int nc_put_var_double(int ncid, int varid, double op) {
         return nc_put_var_double(ncid, varid, new double[] { op });
     }
 
 //int
 //nc_get_var_double(int ncid, int varid, double *ip);
 
 //int
 //nc_put_var_ubyte(int ncid, int varid, const unsigned char *op);
     public static native int nc_put_var_ubyte(int ncid, int varid, ByteByReference op);
     public static native int nc_put_var_ubyte(int ncid, int varid, ByteBuffer op);
     public static native int nc_put_var_ubyte(int ncid, int varid, byte[] op);
     public static int nc_put_var_ubyte(int ncid, int varid, byte op) {
         return nc_put_var_ubyte(ncid, varid, new byte[] { op });
     }
 
 //int
 //nc_get_var_ubyte(int ncid, int varid, unsigned char *ip);
 
 //int
 //nc_put_var_ushort(int ncid, int varid, const unsigned short *op);
 
 //int
 //nc_get_var_ushort(int ncid, int varid, unsigned short *ip);
 
 //int
 //nc_put_var_uint(int ncid, int varid, const unsigned int *op);
 
 //int
 //nc_get_var_uint(int ncid, int varid, unsigned int *ip);
 
 //int
 //nc_put_var_longlong(int ncid, int varid, const long long *op);
 
 //int
 //nc_get_var_longlong(int ncid, int varid, long long *ip);
 
 //int
 //nc_put_var_ulonglong(int ncid, int varid, const unsigned long long *op);
 
 //int
 //nc_get_var_ulonglong(int ncid, int varid, unsigned long long *ip);
 
 //int
 //nc_put_var_string(int ncid, int varid, const char **op);
 
 //int
 //nc_get_var_string(int ncid, int varid, char **ip);
 
 //#ifdef LOGGING
 
 /* Set the log level. 0 shows only errors, 1 only major messages,
  * etc., to 5, which shows way too much information. */
 //int
 //nc_set_log_level(int new_level);
 
 /* Use this to turn off logging by calling
    nc_log_level(NC_TURN_OFF_LOGGING) */
 //#define NC_TURN_OFF_LOGGING (-1)
 
 /* Show the netCDF library's in-memory metadata for a file. */
 //int
 //nc_show_metadata(int ncid);
 
 //#else /* not LOGGING */
 
 //#define nc_show_metadata(e)
 //#define nc_set_log_level(e)
 
 //#endif /* LOGGING */
 
 /* End {put,get}_var */
 
 /* #ifdef _CRAYMPP */
 /*
  * Public interfaces to better support
  * CRAY multi-processor systems like T3E.
  * A tip of the hat to NERSC.
  */
 /*
  * It turns out we need to declare and define
  * these public interfaces on all platforms
  * or things get ugly working out the
  * FORTRAN interface. On !_CRAYMPP platforms,
  * these functions work as advertised, but you
  * can only use "processor element" 0.
  */
 
 //int
 //nc__create_mp(const char *path, int cmode, size_t initialsz, int basepe,
 //        size_t *chunksizehintp, int *ncidp);
 
 //int
 //nc__open_mp(const char *path, int mode, int basepe,
 //        size_t *chunksizehintp, int *ncidp);
 
 //int
 //nc_delete(const char * path);
 
 //int
 //nc_delete_mp(const char * path, int basepe);
 
 //int
 //nc_set_base_pe(int ncid, int pe);
 
 //int
 //nc_inq_base_pe(int ncid, int *pe);
 
 /* #endif _CRAYMPP */
 
 /* Begin v2.4 backward compatiblity */
 /*
  * defining NO_NETCDF_2 to the preprocessor
  * turns off backward compatiblity declarations.
  */
 //#ifndef NO_NETCDF_2
 
 /*
  * Backward compatible aliases
  */
 //#define FILL_BYTE	NC_FILL_BYTE
 //#define FILL_CHAR	NC_FILL_CHAR
 //#define FILL_SHORT	NC_FILL_SHORT
 //#define FILL_LONG	NC_FILL_INT
 //#define FILL_FLOAT	NC_FILL_FLOAT
 //#define FILL_DOUBLE	NC_FILL_DOUBLE
 
 //#define MAX_NC_DIMS	NC_MAX_DIMS
 //#define MAX_NC_ATTRS	NC_MAX_ATTRS
 //#define MAX_NC_VARS	NC_MAX_VARS
 //#define MAX_NC_NAME	NC_MAX_NAME
 //#define MAX_VAR_DIMS	NC_MAX_VAR_DIMS
 
 
 /*
  * Global error status
  */
 //int ncerr;
 
 //#define NC_ENTOOL       NC_EMAXNAME   /* Backward compatibility */
 //#define	NC_EXDR		(-32)	/* */
 //#define	NC_SYSERR	(-31)
 
 /*
  * Global options variable.
  * Used to determine behavior of error handler.
  */
 //#define	NC_FATAL	1
 //#define	NC_VERBOSE	2
 
 //int ncopts;	/* default is (NC_FATAL | NC_VERBOSE) */
 
 //void
 //nc_advise(const char *cdf_routine_name, int err, const char *fmt,...);
 
 /*
  * C data type corresponding to a netCDF NC_LONG argument,
  * a signed 32 bit object.
  *
  * This is the only thing in this file which architecture dependent.
  */
 //typedef int nclong;
 
 //int
 //nctypelen(nc_type datatype);
 
 //int
 //nccreate(const char* path, int cmode);
 
 //int
 //ncopen(const char* path, int mode);
 
 //int
 //ncsetfill(int ncid, int fillmode);
 
 //int
 //ncredef(int ncid);
 
 //int
 //ncendef(int ncid);
 
 //int
 //ncsync(int ncid);
 
 //int
 //ncabort(int ncid);
 
 //int
 //ncclose(int ncid);
 
 //int
 //ncinquire(int ncid, int *ndimsp, int *nvarsp, int *nattsp, int *unlimdimp);
 
 //int
 //ncdimdef(int ncid, const char *name, long len);
 
 //int
 //ncdimid(int ncid, const char *name);
 
 //int
 //ncdiminq(int ncid, int dimid, char *name, long *lenp);
 
 //int
 //ncdimrename(int ncid, int dimid, const char *name);
 
 //int
 //ncattput(int ncid, int varid, const char *name, nc_type xtype,
 //        int len, const void *op);
 
 //int
 //ncattinq(int ncid, int varid, const char *name, nc_type *xtypep, int *lenp);
 
 //int
 //ncattget(int ncid, int varid, const char *name, void *ip);
 
 //int
 //ncattcopy(int ncid_in, int varid_in, const char *name, int ncid_out,
 //        int varid_out);
 
 //int
 //ncattname(int ncid, int varid, int attnum, char *name);
 
 //int
 //ncattrename(int ncid, int varid, const char *name, const char *newname);
 
 //int
 //ncattdel(int ncid, int varid, const char *name);
 
 //int
 //ncvardef(int ncid, const char *name, nc_type xtype,
 //        int ndims, const int *dimidsp);
 
 //int
 //ncvarid(int ncid, const char *name);
 
 //int
 //ncvarinq(int ncid, int varid, char *name, nc_type *xtypep,
 //        int *ndimsp, int *dimidsp, int *nattsp);
 
 //int
 //ncvarput1(int ncid, int varid, const long *indexp, const void *op);
 
 //int
 //ncvarget1(int ncid, int varid, const long *indexp, void *ip);
 
 //int
 //ncvarput(int ncid, int varid, const long *startp, const long *countp,
 //        const void *op);
 
 //int
 //ncvarget(int ncid, int varid, const long *startp, const long *countp,
 //        void *ip);
 
 //int
 //ncvarputs(int ncid, int varid, const long *startp, const long *countp,
 //        const long *stridep, const void *op);
 
 //int
 //ncvargets(int ncid, int varid, const long *startp, const long *countp,
 //        const long *stridep, void *ip);
 
 //int
 //ncvarputg(int ncid, int varid, const long *startp, const long *countp,
 //        const long *stridep, const long *imapp, const void *op);
 
 //int
 //ncvargetg(int ncid, int varid, const long *startp, const long *countp,
 //        const long *stridep, const long *imapp, void *ip);
 
 //int
 //ncvarrename(int ncid, int varid, const char *name);
 
 //int
 //ncrecinq(int ncid, int *nrecvarsp, int *recvaridsp, long *recsizesp);
 
 //int
 //ncrecget(int ncid, long recnum, void **datap);
 
 //int
 //ncrecput(int ncid, long recnum, void *const *datap);
 
     // quick, inefficient hack to suppory size_t*
     private static Buffer toBuffer(NativeLong[] nativeLongs) {
         if (NativeLong.SIZE == 4) {
             IntBuffer ib = IntBuffer.allocate(nativeLongs.length);
             for (int i = 0; i < nativeLongs.length; ++i) {
                 ib.put(nativeLongs[i].intValue());
             }
             return ib;
         } else {
             LongBuffer lb = LongBuffer.allocate(nativeLongs.length);
             for (int i = 0; i < nativeLongs.length; ++i) {
                 lb.put(nativeLongs[i].longValue());
             }
             return lb;
         }
     }
 
 }
