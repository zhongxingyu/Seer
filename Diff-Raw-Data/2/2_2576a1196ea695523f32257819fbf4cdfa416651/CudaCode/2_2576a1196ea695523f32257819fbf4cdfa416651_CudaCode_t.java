 package codegen;
 import ast.*;
 import java.util.*;
 /*
  * Helpful class containing all our C++ code that we output
  */
 public class CudaCode
 {
 	/*
 	 * Return the code for all the pregenned helpers
 	 */
 	public static String helpers()
 	{
 		return cudaTest()+printProgress()+rtclock();
 	}
 	public static String cudaTest()
 	{
 		return "static unsigned CudaTest(char *msg)"+
 			"{"+
 			"cudaError_t e;"+
 			"cudaThreadSynchronize();"+
 			"if (cudaSuccess != (e = cudaGetLastError())) {"+
 			"fprintf(stderr, \"%s: %d\\n\", msg, e);"+
 			"fprintf(stderr, \"%s\\n\", cudaGetErrorString(e));"+
 			"exit(-1);"+
 			"}"+
 			"}";
 
 	}
 	private static String printProgress()
 	{
 		return
 
 			"void progressPrint(unsigned maxii, unsigned ii) {"+
 			"const unsigned nsteps = 10;"+
 			"unsigned ineachstep = (maxii / nsteps);"+
 			"if (ii % ineachstep == 0) {"+
 			"printf(\"\\t%3d%%\\r\", ii*100/maxii + 1);"+
 			"fflush(stdout);"+
 			"}"+
 			"}";
 	}
 	private static String rtclock()
 	{
 		return "double rtclock()"+
 			"{"+
 			"struct timezone Tzp;"+
 			"struct timeval Tp;"+
 			"int stat;"+
 			"stat = gettimeofday (&Tp, &Tzp);"+
 			"if (stat != 0) printf(\"Error return from gettimeofday: %d\",stat);"+
 			"return(Tp.tv_sec + Tp.tv_usec*1.0e-6);"+
 			"}";
 
 	}
 	public static String globals(List<AttributeDef> node_attributes,List<AttributeDef> edge_attributes)
 	{
 		//NOTE: We only support one edge attribute right now until
 		//we define how the .gr format will work with >1
 		String edge_attr = "attribute_e_";
 		for (AttributeDef def : edge_attributes) {
 			//src and dst are handled implicitly
 			if(def.id.id.equals("src") || def.id.id.equals("dst"))
 				continue;
 			edge_attr += def.id.id;
 			break;
 		}
 		String nodes = "";
 		for(AttributeDef def : node_attributes) {
 			if(def.id.id.equals("node"))
 				continue;
 			nodes += "__device__ int *_attribute_n_"+def.id.id+";\n";
 		}
 		return 
 			"texture <unsigned,cudaTextureType1D,cudaReadModeElementType>_destination;"+
 			"texture <unsigned,cudaTextureType1D,cudaReadModeElementType>_psrc;"+
 			"texture <unsigned,cudaTextureType1D,cudaReadModeElementType>_noutgoing;"+
 			"texture <unsigned,cudaTextureType1D,cudaReadModeElementType>_srcsrc;"+
 			"__device__ int * _"+edge_attr +";\n"+
 			"__device__ bool _gchanged;\n"+
 			"unsigned num_edges;\n"+
 			"unsigned num_nodes;\n"+
 			"unsigned THREADS;unsigned GRID;\n"+nodes;
 
 	}
 	public static String getNode()
 	{
 		return "__device__ inline Node* _get_node(int id){return &_graph[id];}";
 	}
 	public static String edge()
 	{
 		//For now let's make edge a noop
 		return "__device__ inline bool _edge(Node *a, Node *b) {return true;}\n";
 	}
 	public static String headers()
 	{
 		return "#include <stdio.h>\n"+
 			"#include <time.h>\n"+
 			"#include <fstream>\n"+
 			"#include <string>\n"+
 			"#include <iostream>\n"+
 			"#include <limits>\n"+
 			"#include <string.h>\n"+
 			""+
 			"#include <unistd.h>\n"+
 			"#include <cassert>\n"+
 			"#include <inttypes.h>\n"+
 			"#include <unistd.h>\n"+
 			"#include <stdio.h>\n"+
 			"#include <time.h>\n"+
 			"#include <sys/time.h>\n"+
 			"#include <stdlib.h>\n"+
 			"#include <stdarg.h>\n"+
 			"#include <sys/mman.h>\n"+
 			"#include <sys/stat.h>\n"+
 			"#include <sys/types.h>\n"+
 			"#include <fcntl.h>\n"+
 			"#include <unistd.h>\n"+
 			"#include <cassert>\n"+
			"#include <inttypes.h>\n";
 	}
 	public static String genMain()
 	{
 		String main =
 			"int main(int argc, char **argv)"+
 			"{"+
 			"load_graph(argv[1]);"+
 			"printf(\"Graph loaded\\nSolving...\\n\");"+
 			"THREADS=512;"+
 			"GRID = (num_nodes+THREADS-1)/THREADS;"+
 			"double start = rtclock();"+
 			"_action_main();"+
 			"double end = rtclock();"+
 			"printf(\"Total Elapsed:%.3lf ms\\n\",100*(end-start));"+
 			"return 0;" +
 			"}\n";
 		return main;
 	}
 	public static String loadGraph(List<AttributeDef> node_attributes,List<AttributeDef> edge_attributes)
 	{
 		//NOTE: We only support one edge attribute right now until
 		//we define how the .gr format will work with >1
 		String edge_attr = "";
 		for (AttributeDef def : edge_attributes) {
 			//src and dst are handled implicitly
 			if(def.id.id.equals("src") || def.id.id.equals("dst"))
 				continue;
 			edge_attr += def.id.id;
 			break;
 		}
 		String base= ""+
 			"int load_graph(char* file)"+
 			"{"+
 			""+
 			"std::ifstream cfile;"+
 			"cfile.open(file);"+
 			""+
 			"int masterFD = open(file, O_RDONLY);"+
 			"if (masterFD == -1) {"+
 			"printf(\"FileGraph::structureFromFile: unable to open %s.\\n\", file);"+
 			"return 1;"+
 			"}"+
 			""+
 			"struct stat buf;"+
 			"int f = fstat(masterFD, &buf);"+
 			"if (f == -1) {"+
 			"printf(\"FileGraph::structureFromFile: unable to stat %s.\\n\", file);"+
 			"abort();"+
 			"}"+
 			"size_t masterLength = buf.st_size;"+
 			""+
 			"int _MAP_BASE = MAP_PRIVATE;"+
 			"void* m = mmap(0, masterLength, PROT_READ, _MAP_BASE, masterFD, 0);"+
 			"if (m == MAP_FAILED) {"+
 			"m = 0;"+
 			"printf(\"FileGraph::structureFromFile: mmap failed.\\n\");"+
 			"abort();"+
 			"}"+
 			""+
 			"uint64_t* fptr = (uint64_t*)m;"+
 			"__attribute__((unused)) uint64_t version = le64toh(*fptr++);"+
 			"assert(version == 1);"+
 			"uint64_t sizeEdgeTy = le64toh(*fptr++);"+
 			"uint64_t numNodes = le64toh(*fptr++);"+
 			"uint64_t numEdges = le64toh(*fptr++);"+
 			"uint64_t *outIdx = fptr;"+
 			"fptr += numNodes;"+
 			"uint32_t *fptr32 = (uint32_t*)fptr;"+
 			"uint32_t *outs = fptr32; "+
 			"fptr32 += numEdges;"+
 			"if (numEdges % 2) fptr32 += 1;"+
 			"unsigned  *edgeData = (unsigned *)fptr32;"+
 			"num_nodes = numNodes;"+
 			"num_edges = numEdges;"+
 			"printf(\"file %s: nnodes=%d, nedges=%d.\\n\", file, num_nodes, num_edges);"+
 			"unsigned edge_index = 1;"+
 
 
 
 			"unsigned int *destination = (unsigned int *)malloc((num_edges+1) * sizeof(unsigned int));"+
 			"unsigned *psrc = (unsigned int *)calloc(num_nodes+1, sizeof(unsigned int));"+
 			"psrc[num_nodes] = num_edges;"+
 			"unsigned *noutgoing = (unsigned int *)calloc(num_nodes, sizeof(unsigned int));"+
 			"unsigned *srcsrc = (unsigned int *)malloc(num_nodes * sizeof(unsigned int));"+
 			//ALLOC new defs
 			"unsigned *attribute_"+edge_attr+ "= (unsigned int *)calloc(num_edges,sizeof(unsigned int));"+
 
 
 			"for (unsigned ii = 0; ii < num_nodes; ++ii) {"+
 			"srcsrc[ii] = ii;"+
 			"if (ii > 0) {"+
 			"psrc[ii] = le64toh(outIdx[ii - 1]) + 1;"+
 			"noutgoing[ii] = le64toh(outIdx[ii]) - le64toh(outIdx[ii - 1]);"+
 			"} else {"+
 			"psrc[0] = 1;"+
 			"noutgoing[0] = le64toh(outIdx[0]);"+
 			"}"+
 			"for (unsigned jj = 0; jj < noutgoing[ii]; ++jj) {"+
 			"unsigned edgeindex = psrc[ii] + jj;"+
 			"unsigned dst = le32toh(outs[edgeindex - 1]);"+
 			"if (dst >= num_nodes) printf(\"\\tinvalid edge from %d to %d at index %d(%d).\\n\", ii, dst, jj, edgeindex);"+
 			"destination[edgeindex] = dst;"+
 			"attribute_"+edge_attr+"[edgeindex] = edgeData[edgeindex - 1];"+
 			""+
 			//"++nincoming[dst];"+
 			"}"+
 			"progressPrint(num_nodes, ii);"+
 			"}"+
 
 
 
 			"cfile.close();"+
 			"unsigned int *d_destination, *d_psrc, *d_noutgoing, *d_srcsrc, *d_attribute_"+edge_attr+";"+
 			"cudaMalloc((void**)&d_destination,sizeof(unsigned)*num_edges);"+
 			"cudaMalloc((void**)&d_psrc,sizeof(unsigned)*num_nodes);"+
 			"cudaMalloc((void**)&d_noutgoing,sizeof(unsigned)*num_nodes);"+
 			"cudaMalloc((void**)&d_srcsrc,sizeof(unsigned)*num_nodes);"+
 			"cudaMalloc((void**)&d_attribute_"+edge_attr+",sizeof(unsigned)*num_edges);"+
 			"CudaTest(\"Cuda Mallocs\\n\");"+
 			"printf(\"Copying to device...\\n\");"+
 			"cudaMemcpy((void*)d_destination,destination,sizeof(unsigned)*num_edges,cudaMemcpyHostToDevice);"+
 			"cudaMemcpy((void*)d_attribute_"+edge_attr+",attribute_"+edge_attr+",sizeof(unsigned)*num_edges,cudaMemcpyHostToDevice);"+
 			"cudaMemcpy((void*)d_psrc,psrc,sizeof(unsigned)*num_nodes,cudaMemcpyHostToDevice);"+
 			"cudaMemcpy((void*)d_noutgoing,noutgoing,sizeof(unsigned)*num_nodes,cudaMemcpyHostToDevice);"+
 			"cudaMemcpy((void*)d_srcsrc,srcsrc,sizeof(unsigned)*num_nodes,cudaMemcpyHostToDevice);"+
 			"CudaTest(\"Cuda Memcpy\\n\");"+
 			"cudaBindTexture((size_t)0,_destination,d_destination,sizeof(unsigned) * num_edges);"+
 			"cudaBindTexture((size_t)0,_psrc,d_psrc,sizeof(unsigned) * num_nodes);"+
 			"cudaBindTexture((size_t)0,_psrc,d_psrc,sizeof(unsigned) * num_nodes);"+
 			"cudaBindTexture((size_t)0,_noutgoing,d_noutgoing,sizeof(unsigned) * num_nodes);"+
 			"cudaBindTexture((size_t)0,_srcsrc,d_srcsrc,sizeof(unsigned) * num_nodes);"+
 			"CudaTest(\"Cuda BindTexture\\n\");"+
 			"cudaMemcpyToSymbol(_attribute_e_weight, (void *) &d_attribute_weight, sizeof(unsigned *), 0, cudaMemcpyHostToDevice);";
 		String nodes = "";
 		for(AttributeDef def : node_attributes) {
 			if(def.id.id.equals("node"))
 				continue;
 			nodes += "unsigned *d_"+def.id.id+";"+
 				"cudaMalloc((void**)&d_"+def.id.id+",sizeof(unsigned)*num_nodes);"+
 				"cudaMemcpyToSymbol(_attribute_n_"+def.id.id+",(void*)&d_"+def.id.id+",sizeof(unsigned*));";
 		}
 
 		return base + nodes +
 			"return 0;"+
 			"}";
 	}
 }
